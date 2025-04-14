package com.jcca.common.webssh.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.utils.AppLogUtils;
import com.jcca.common.webssh.constant.ConstantPool;
import com.jcca.common.webssh.pojo.ConnectInfo;
import com.jcca.common.webssh.pojo.WebRemoteData;
import com.jcca.common.webssh.service.WebSocketSSHService;
import com.jcraft.jsch.*;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;

/**
 * @Description: WebSSH业务逻辑实现
 * @Author: NoCortY
 * @Date: 2020/3/8
 */
@Service
public class WebSocketSSHSSHImpl implements WebSocketSSHService {
    private static Map<String, ConnectInfo> sshMap = new ConcurrentHashMap<>();
    private ExecutorService executorService = Executors.newCachedThreadPool();

    /**
     * @Description: 初始化连接
     * @Param: [session]
     * @return: void
     * @Author: NoCortY
     * @Date: 2020/3/7
     */
    @Override
    public void initConnection(WebSocketSession session, String username) {
        JSch jSch = new JSch();
        ConnectInfo connectInfo = new ConnectInfo();
        connectInfo.setJSch(jSch);
        connectInfo.setWebSocketSession(session);
        sshMap.put(username, connectInfo);
    }

    /**
     * @Description: 处理客户端发送的数据
     * @Param: [buffer, session]
     * @return: void
     * @Author: NoCortY
     * @Date: 2020/3/7
     */
    @Override
    public void recvHandle(String buffer, WebSocketSession session) {
        ObjectMapper objectMapper = new ObjectMapper();
        WebRemoteData webRemoteData = null;
        try {
            webRemoteData = objectMapper.readValue(buffer, WebRemoteData.class);
        } catch (IOException e) {
            AppLogUtils.buildLogError(LogFunctionEnum.REMOTE_CONNECT, "SSH读取前端数据异常", e.getMessage());
            return;
        }
        String itsmUsername = webRemoteData.getItsmUsername();
        if (ConstantPool.WEBSSH_OPERATE_CONNECT.equals(webRemoteData.getOperate())) {
            ConnectInfo connectInfo = sshMap.get(itsmUsername);
            WebRemoteData finalWebRemoteData = webRemoteData;
            executorService.execute(() -> {
                try {
                    connectToSSH(connectInfo, finalWebRemoteData, session);
                    if (connectInfo.getChannel().isClosed()) {
                        close(session, itsmUsername);
                    }
                } catch (JSchException | IOException e) {
                    AppLogUtils.buildLogError(LogFunctionEnum.REMOTE_CONNECT, "ssh连接异常", e.getMessage());
                    try {
                        sendMessage(session, ("ERROR : " + e.getMessage()).getBytes());
                    } catch (IOException ex) {
                        AppLogUtils.buildLogError(LogFunctionEnum.REMOTE_CONNECT, "ssh连接发送消息异常", e.getMessage());
                    }
                    close(session, itsmUsername);
                }
            });
        } else if (ConstantPool.WEBSSH_OPERATE_COMMAND.equals(webRemoteData.getOperate())) {
            String command = webRemoteData.getMessage();
            ConnectInfo connectInfo = sshMap.get(itsmUsername);
            if (connectInfo != null) {
                try {
                    ChannelShell channel = (ChannelShell) connectInfo.getChannel();
                    if (channel != null) {
                        channel.setPtySize(webRemoteData.getCols(), webRemoteData.getRows(), webRemoteData.getWidth(), webRemoteData.getHeight());
                        transToSSH(webRemoteData, channel, command);
                        if (channel.isClosed()) {
                            close(session, itsmUsername);
                        }
                    }
                } catch (IOException e) {
                    AppLogUtils.buildLogError(LogFunctionEnum.REMOTE_CONNECT, "ssh连接异常", e.getMessage());
                    try {
                        sendMessage(session, ("ERROR : " + e.getMessage()).getBytes());
                    } catch (IOException ex) {
                        AppLogUtils.buildLogError(LogFunctionEnum.REMOTE_CONNECT, "消息发送异常", e.getMessage());

                    }
                    close(session, itsmUsername);
                }
            }
        } else if (ConstantPool.WEBSSH_OPERATE_HEARTBEAT.equals(webRemoteData.getOperate())) {
            ConnectInfo connectInfo = sshMap.get(itsmUsername);
            if (connectInfo != null) {
                try {
                    if (connectInfo.getChannel().isConnected())
                        sendMessage(session, "OK".getBytes());
                } catch (IOException e) {
                    AppLogUtils.buildLogError(LogFunctionEnum.REMOTE_CONNECT, "消息发送失败", e.getMessage());
                }
            }
        } else {
            AppLogUtils.buildLogError(LogFunctionEnum.REMOTE_CONNECT, "SSH不支持的操作", itsmUsername);
            close(session, itsmUsername);
        }
    }

    @Override
    public void sendMessage(WebSocketSession session, byte[] buffer) throws IOException {
        session.sendMessage(new TextMessage(buffer));
    }

    @Override
    public void close(WebSocketSession session, String username) {
        ConnectInfo connectInfo = sshMap.get(username);
        if (connectInfo != null) {
            if (connectInfo.getChannel() != null) {
                connectInfo.getChannel().disconnect();
            }
            sshMap.remove(username);
        }
        try {
            session.close();
        } catch (IOException e) {
            AppLogUtils.buildLogError(LogFunctionEnum.REMOTE_CONNECT, "websocket远程连接关闭异常", e.getMessage());
        }
    }

    /**
     * @Description: 使用jsch连接终端
     * @Param: [cloudSSH, webSSHData, webSocketSession]
     * @return: void
     * @Author: NoCortY
     * @Date: 2020/3/7
     */
    private void connectToSSH(ConnectInfo connectInfo, WebRemoteData webRemoteData, WebSocketSession webSocketSession) throws JSchException, IOException {
        Session session = null;
        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        session = connectInfo.getJSch().getSession(webRemoteData.getUsername(), webRemoteData.getHost(), webRemoteData.getPort() == null ? 22 : webRemoteData.getPort());
        session.setConfig(config);
        session.setPassword(webRemoteData.getPasswd());
        session.connect(30000);

        //开启shell通道
        Channel channels = session.openChannel("shell");
        ChannelShell channel = (ChannelShell) channels;
        channel.setPtySize(webRemoteData.getCols(), webRemoteData.getRows(), webRemoteData.getWidth(), webRemoteData.getHeight());
        channel.connect(3000);

        connectInfo.setChannel(channel);

        transToSSH(webRemoteData, channel, "\n");

        //读取终端返回的信息流
        try (InputStream inputStream = channel.getInputStream()) {
            byte[] buffer = new byte[1024];
            int i = 0;
            while ((i = inputStream.read(buffer)) != -1) {
                sendMessage(webSocketSession, Arrays.copyOfRange(buffer, 0, i));
            }
        } finally {
            //断开连接后关闭会话
            session.disconnect();
            channel.disconnect();
        }
    }

    private StringBuilder sb = new StringBuilder();

    /**
     * @Description: 将消息转发到终端
     * @Param: [channel, data]
     * @return: void
     * @Author: NoCortY
     * @Date: 2020/3/7
     */
    private void transToSSH(WebRemoteData webRemoteData, Channel channel, String command) throws IOException {
        if (channel != null) {
            if (command.contains("\r")) {
                sb.append(command);
                Matcher matcher = ConstantPool.CTRL_PATTERN.matcher(sb.toString());
                String sanitized = "";
                while (matcher.find()) {
                    sanitized = matcher.replaceAll(replaceMatcher(matcher));
                }
                AppLogUtils.buildLogInfo(LogFunctionEnum.REMOTE_CONNECT,
                        "用户：" + webRemoteData.getItsmUsername() + "，SSH远程IP：" + webRemoteData.getHost(), sanitized);
                sb = new StringBuilder();
            } else {
                sb.append(command);
            }
            OutputStream outputStream = channel.getOutputStream();
            outputStream.write(command.getBytes(StandardCharsets.UTF_8));
            outputStream.flush();
        }
    }

    private String replaceMatcher(Matcher m) {
        int code = m.group().charAt(0);
        String name = ConstantPool.CTRL_NAMES.getOrDefault(code,
                String.format("CTRL_0x%02X", code));
        return "<" + name + ">";
    }
}
