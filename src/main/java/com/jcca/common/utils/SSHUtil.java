package com.jcca.common.utils;

import com.jcraft.jsch.*;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

/**
 * @author hanwone
 */
public class SSHUtil {
    /**
     * 设置编码格式
     */
    private String charset = "UTF-8";
    /**
     * 用户名
     */
    private String username;
    /**
     * 登录密码
     */
    private String password;
    /**
     * 主机IP
     */
    private String host;
    /**
     * 端口
     */
    private int port;
    /**
     * 3秒超时时间
     */
    private static Integer TIME_OUT = 3000;

    private Session session;

    /**
     * @param username 用户名
     * @param password 密码
     * @param host     主机IP
     */
    public SSHUtil(String username, String password, String host, int port) {
        this.username = username;
        this.password = password;
        this.host = host;
        this.port = port;
    }

    /**
     * 连接到指定的IP
     *
     * @throws JSchException
     */
    public void connect() throws JSchException {
        JSch jsch = new JSch();
        session = jsch.getSession(username, host, port);
        session.setPassword(password);
        java.util.Properties config = new java.util.Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);
        session.setTimeout(TIME_OUT);
        session.connect();
    }

    /**
     * 关闭连接
     */
    public void disconnect() throws Exception {
        if (session != null && session.isConnected()) {
            session.disconnect();
        }
    }

    /**
     * 执行一条命令
     */
    public StringBuilder execCmd(String command) throws Exception {
        BufferedReader reader = null;
        Channel channel = null;

        StringBuilder sb = new StringBuilder();

        String channelCommand = "exec";
        channel = session.openChannel(channelCommand);
        ((ChannelExec) channel).setCommand(command);
        channel.setInputStream(null);
        ((ChannelExec) channel).setErrStream(System.err);
        channel.connect();
        InputStream in = channel.getInputStream();
        reader = new BufferedReader(new InputStreamReader(in, Charset.forName(charset)));
        String buf = null;

        while ((buf = reader.readLine()) != null) {
            sb.append(buf).append("  ");
        }
        channel.disconnect();
        return sb;
    }
}
