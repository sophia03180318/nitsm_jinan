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
import org.springframework.util.StringUtils;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
    public void initConnection(WebSocketSession session) {
        JSch jSch = new JSch();
        ConnectInfo connectInfo = new ConnectInfo();
        connectInfo.setJSch(jSch);
        connectInfo.setWebSocketSession(session);
        String path = Objects.requireNonNull(session.getUri()).getPath();
        String username = path.substring(path.lastIndexOf("/") + 1);
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
        if (StringUtils.isEmpty(webRemoteData.getMessage())) {
            webRemoteData.setOperate(ConstantPool.WEBSSH_OPERATE_CONNECT);
        } else {
            webRemoteData.setOperate(ConstantPool.WEBSSH_OPERATE_COMMAND);
        }
        String itsmUsername = webRemoteData.getItsmUsername();
        if (ConstantPool.WEBSSH_OPERATE_CONNECT.equals(webRemoteData.getOperate())) {
            ConnectInfo connectInfo = sshMap.get(itsmUsername);
            WebRemoteData finalWebRemoteData = webRemoteData;
            executorService.execute(() -> {
                try {
                    connectToSSH(connectInfo, finalWebRemoteData, session);
                    if (connectInfo.getChannel().isClosed()) {
                        close(session);
                    }
                } catch (JSchException | IOException e) {
                    AppLogUtils.buildLogError(LogFunctionEnum.REMOTE_CONNECT, "ssh连接异常", e.getMessage());
                    try {
                        sendMessage(session, ("ERROR : " + e.getMessage()).getBytes());
                    } catch (IOException ex) {
                        AppLogUtils.buildLogError(LogFunctionEnum.REMOTE_CONNECT, "ssh连接发送消息异常", e.getMessage());
                    }
                    close(session);
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
                        transToSSH(channel, command);
                        if (channel.isClosed()) {
                            close(session);
                        }
                    }
                } catch (IOException e) {
                    AppLogUtils.buildLogError(LogFunctionEnum.REMOTE_CONNECT, "ssh连接异常", e.getMessage());
                    try {
                        sendMessage(session, ("ERROR : " + e.getMessage()).getBytes());
                    } catch (IOException ex) {
                        AppLogUtils.buildLogError(LogFunctionEnum.REMOTE_CONNECT, "消息发送异常", e.getMessage());

                    }
                    close(session);
                }
            }
        } else if (ConstantPool.WEBSSH_OPERATE_HEARTBEAT.equals(webRemoteData.getOperate())) {
            //检查心跳
            ConnectInfo connectInfo = sshMap.get(itsmUsername);
            if (connectInfo != null) {
                try {
                    //处于连接状态则发送健康数据，不能为空，空则断开连接。
                    if (connectInfo.getChannel().isConnected())
                        sendMessage(session, "Heartbeat healthy".getBytes());
                } catch (IOException e) {
                    AppLogUtils.buildLogError(LogFunctionEnum.REMOTE_CONNECT, "消息发送失败", e.getMessage());
                }
            }
        } else {
            AppLogUtils.buildLogError(LogFunctionEnum.REMOTE_CONNECT, "不支持的操作", itsmUsername);
            close(session);
        }
    }

    @Override
    public void sendMessage(WebSocketSession session, byte[] buffer) throws IOException {
        session.sendMessage(new TextMessage(buffer));
    }

    @Override
    public void close(WebSocketSession session) {
        String path = Objects.requireNonNull(session.getUri()).getPath();
        String username = path.substring(path.lastIndexOf("/") + 1);
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
        session = connectInfo.getJSch().getSession(webRemoteData.getUsername(), webRemoteData.getHost(), webRemoteData.getPort());
        session.setConfig(config);
        session.setPassword(webRemoteData.getPasswd());
        session.connect(30000);

        //开启shell通道
        Channel channels = session.openChannel("shell");
        ChannelShell channel = (ChannelShell) channels;
        channel.setPtySize(webRemoteData.getCols(), webRemoteData.getRows(), webRemoteData.getWidth(), webRemoteData.getHeight());
        channel.connect(3000);

        connectInfo.setChannel(channel);

        transToSSH(channel, "\n");

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

    /**
     * @Description: 将消息转发到终端
     * @Param: [channel, data]
     * @return: void
     * @Author: NoCortY
     * @Date: 2020/3/7
     */
    private void transToSSH(Channel channel, String command) throws IOException {
        if (channel != null) {
            OutputStream outputStream = channel.getOutputStream();
            outputStream.write(command.getBytes());
            outputStream.flush();
        }
    }
}
