package com.jcca.web.websocket.util;

import cn.hutool.json.JSONUtil;
import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.utils.AppLogUtils;
import com.jcca.web.websocket.RemoteConnetDto;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.jcca.web.websocket.WebRemoteConnect.SESSION_POOL;

public class SshRemoteUtil {

    private static final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private static final Map<String, ChannelShell> shellMap = new ConcurrentHashMap<>();

    /**
     * 连接到远程服务器
     *
     * @param host     主机地址
     * @param port     端口号
     * @param username 用户名
     * @param password 密码
     * @throws JSchException 连接异常
     */
    public static Session connect(String host, int port, String username, String password) throws JSchException {
        JSch jsch = new JSch();
        Session session = jsch.getSession(username, host, port);
        session.setPassword(password);
        java.util.Properties config = new java.util.Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);
        session.setTimeout(30000);
        session.connect();
        return session;
    }

    public static void shellConnect(RemoteConnetDto dto, Session session) throws JSchException {
        ChannelShell channel = (ChannelShell) session.openChannel("shell");
        channel.connect(3000);
        channel.setPtySize(80, 24, 640, 480);
        shellMap.put(dto.getItsmUsername(), channel);

        executorService.execute(() -> {
            InputStream inputStream = null;
            byte[] tmp = new byte[1024];
            int i;
            try {
                inputStream = channel.getInputStream();
                while ((i = inputStream.read(tmp, 0, 1024)) != -1) {
                    String msg = new String(tmp, 0, i);
                    msg = msg.replace(temComd + "\r\n", "");
                    if (StringUtils.isEmpty(msg)) continue;
                    AppLogUtils.buildLogInfo(LogFunctionEnum.REMOTE_CONNECT, "SSH命令执行结果", msg);
                    dto.setMessage(msg);
                    SESSION_POOL.get(dto.getItsmUsername()).getBasicRemote().sendText(JSONUtil.toJsonStr(dto));
                }
            } catch (Exception e) {
                disconnect(session);
                shellMap.remove(dto.getItsmUsername());
                AppLogUtils.buildLogError(LogFunctionEnum.REMOTE_CONNECT, "SSH远程连接读取数据异常", dto);
            } finally {
                try {
                    assert inputStream != null;
                    inputStream.close();
                } catch (Exception e) {
                    disconnect(session);
                    AppLogUtils.buildLogError(LogFunctionEnum.REMOTE_CONNECT, "SSH远程连接流关闭异常", dto);
                }
                channel.disconnect();
            }
        });
    }

    private static String temComd = "";

    public static void execCommand(String itsmUsername, String command) {
        temComd = command;
        try {
            ChannelShell shell = shellMap.get(itsmUsername);
            if (Objects.isNull(shell)) {
                return;
            }
            if (!shell.isConnected()) {
                shellMap.remove(itsmUsername);
                return;
            }
            OutputStream os = shell.getOutputStream();
            os.write((command + "\n").getBytes());
            os.flush();
        } catch (IOException e) {
            AppLogUtils.buildLogError(LogFunctionEnum.REMOTE_CONNECT, "SSH远程连接执行命令异常", command);
            shellMap.remove(itsmUsername);
        }
    }

    /**
     * 关闭SSH连接
     */
    public static void disconnect(Session session) {
        if (session != null && session.isConnected()) {
            session.disconnect();
        }
    }
}