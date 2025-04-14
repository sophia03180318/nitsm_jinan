package com.jcca.common.webssh.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.utils.AppLogUtils;
import com.jcca.common.webssh.constant.ConstantPool;
import com.jcca.common.webssh.pojo.ConnectInfo;
import com.jcca.common.webssh.pojo.WebRemoteData;
import com.jcca.common.webssh.service.WebSocketTelnetService;
import org.apache.commons.net.telnet.TelnetClient;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;

/**
 * @author: hhw
 * @description: WebSocketTelnetImpl主要是用来
 * @date: 2025-04-03  13:43
 * @since: 2.1.5.0
 */
@Service
public class WebSocketTelnetImpl implements WebSocketTelnetService {

    private static Map<String, ConnectInfo> telnetMap = new ConcurrentHashMap<>();
    private ExecutorService executorService = Executors.newCachedThreadPool();

    @Override
    public void initConnection(WebSocketSession session, String username) {
        TelnetClient client = new TelnetClient();
        ConnectInfo connectInfo = new ConnectInfo();
        connectInfo.setTelnetClient(client);
        connectInfo.setWebSocketSession(session);

        telnetMap.put(username, connectInfo);
    }

    @Override
    public void recvHandle(String payload, WebSocketSession webSocketSession) {
        ObjectMapper objectMapper = new ObjectMapper();
        WebRemoteData webRemoteData = null;
        try {
            webRemoteData = objectMapper.readValue(payload, WebRemoteData.class);
        } catch (IOException e) {
            AppLogUtils.buildLogError(LogFunctionEnum.REMOTE_CONNECT, "TELNET读取前端数据异常", e.getMessage());
            return;
        }

        String itsmUsername = webRemoteData.getItsmUsername();
        if (ConstantPool.WEBSSH_OPERATE_CONNECT.equals(webRemoteData.getOperate())) {
            ConnectInfo connectInfo = telnetMap.get(itsmUsername);
            TelnetClient telnetClient = connectInfo.getTelnetClient();
            WebRemoteData finalWebRemoteData = webRemoteData;
            executorService.execute(() -> {
                try {
                    telnetClient.connect(finalWebRemoteData.getHost(), finalWebRemoteData.getPort() == null ? 23 : finalWebRemoteData.getPort());
                    InputStream inputStream = telnetClient.getInputStream();
                    byte[] buffer = new byte[1024];
                    int i = 0;
                    while ((i = inputStream.read(buffer)) != -1) {
                        sendMessage(finalWebRemoteData, webSocketSession, Arrays.copyOfRange(buffer, 0, i));
                    }
                } catch (IOException e) {
                    AppLogUtils.buildLogError(LogFunctionEnum.REMOTE_CONNECT, "telnet连接异常", e.getMessage());
                    this.close(webSocketSession, itsmUsername);
                }
            });
        } else if (ConstantPool.WEBSSH_OPERATE_COMMAND.equals(webRemoteData.getOperate())) {
            ConnectInfo connectInfo = telnetMap.get(itsmUsername);
            TelnetClient telnetClient = connectInfo.getTelnetClient();
            OutputStream outputStream = telnetClient.getOutputStream();
            try {
                outputStream.write(webRemoteData.getMessage().getBytes());
                outputStream.flush();
            } catch (IOException e) {
                AppLogUtils.buildLogError(LogFunctionEnum.REMOTE_CONNECT, "telnet读取数据异常", e.getMessage());
                this.close(webSocketSession, itsmUsername);
            }
        } else if (ConstantPool.WEBSSH_OPERATE_HEARTBEAT.equals(webRemoteData.getOperate())) {
            try {
                sendMessage(webRemoteData, webSocketSession, "OK".getBytes());
            } catch (IOException e) {
                AppLogUtils.buildLogError(LogFunctionEnum.REMOTE_CONNECT, "消息发送失败", e.getMessage());
            }
        } else {
            AppLogUtils.buildLogError(LogFunctionEnum.REMOTE_CONNECT, "TELNET不支持的操作", itsmUsername);
            close(webSocketSession, itsmUsername);
        }
    }

    private StringBuilder sb = new StringBuilder();

    @Override
    public void sendMessage(WebRemoteData webRemoteData, WebSocketSession session, byte[] buffer) throws IOException {
        if (session.isOpen()) {
            TextMessage textMessage = new TextMessage(buffer);
            String command = textMessage.getPayload();
            if (command.contains("\n")) {
                sb.append(command);

                Matcher matcher = ConstantPool.CTRL_PATTERN.matcher(sb.toString());
                String sanitized = "";
                while (matcher.find()) {
                    sanitized = matcher.replaceAll(replaceMatcher(matcher));
                }
                AppLogUtils.buildLogInfo(LogFunctionEnum.REMOTE_CONNECT,
                        "用户：" + webRemoteData.getItsmUsername() + "，TELNET远程IP：" + webRemoteData.getHost(), sanitized);
                sb = new StringBuilder();
            } else {
                sb.append(command);
            }
            session.sendMessage(textMessage);
        }
    }

    private String replaceMatcher(Matcher m) {
        int code = m.group().charAt(0);
        String name = ConstantPool.CTRL_NAMES.getOrDefault(code,
                String.format("CTRL_0x%02X", code));
        return "<" + name + ">";
    }

    @Override
    public void close(WebSocketSession session, String username) {
        ConnectInfo connectInfo = telnetMap.get(username);
        try {
            if (connectInfo != null) {
                if (connectInfo.getTelnetClient() != null) {
                    connectInfo.getTelnetClient().disconnect();
                }
                telnetMap.remove(username);
            }

            session.close();
        } catch (IOException e) {
            AppLogUtils.buildLogError(LogFunctionEnum.REMOTE_CONNECT, "websocket远程连接关闭异常", e.getMessage());
        }
    }
}