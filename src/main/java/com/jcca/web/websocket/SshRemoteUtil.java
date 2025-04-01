package com.jcca.web.websocket;

import com.jcraft.jsch.*;

import java.io.InputStream;

public class SshRemoteUtil {

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
        session.setConfig("StrictHostKeyChecking", "no");
        session.connect();
        session.setTimeout(3000);
        return session;
    }

    /**
     * 执行远程服务器命令并返回结果
     *
     * @param session 已建立的SSH会话连接，必须为已成功连接的Session对象
     * @param command 需要在远程服务器上执行的Linux命令字符串
     * @return 命令标准输出的完整内容，包含多行执行结果
     * @throws JSchException       当SSH通道建立失败或连接异常时抛出
     * @throws java.io.IOException 当读取命令输出流发生I/O错误时抛出
     */
    public static String executeCommand(Session session, String command) throws JSchException, java.io.IOException {
        Channel channel = session.openChannel("exec");
        ((ChannelExec) channel).setCommand(command);
        channel.setInputStream(null);
        ((ChannelExec) channel).setErrStream(System.err);

        InputStream in = channel.getInputStream();
        channel.connect();

        StringBuilder output = new StringBuilder();
        byte[] tmp = new byte[1024];
        while (true) {
            while (in.available() > 0) {
                int i = in.read(tmp, 0, 1024);
                if (i < 0) break;
                output.append(new String(tmp, 0, i));
            }
            if (channel.isClosed()) {
                if (in.available() > 0) continue;
                break;
            }
        }
        channel.disconnect();
        return output.toString();
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