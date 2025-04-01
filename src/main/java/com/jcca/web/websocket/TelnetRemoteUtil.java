package com.jcca.web.websocket;

import org.apache.commons.net.telnet.TelnetClient;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

public class TelnetRemoteUtil {

    private TelnetClient telnetClient;

    public TelnetRemoteUtil() {
        this.telnetClient = new TelnetClient();
    }

    /**
     * 连接到远程服务器
     *
     * @param host 主机地址
     * @param port 端口号
     * @throws IOException 连接异常
     */
    public void connect(String host, int port) throws IOException {
        telnetClient.connect(host, port);
    }

    /**
     * 执行远程命令
     *
     * @param command 需要执行的命令
     * @return 命令执行结果
     * @throws IOException IO异常
     */
    public String executeCommand(String command) throws IOException {
        PrintStream out = new PrintStream(telnetClient.getOutputStream());
        out.println(command);
        out.flush();

        InputStream in = telnetClient.getInputStream();
        StringBuilder output = new StringBuilder();
        byte[] buffer = new byte[1024];
        int readLength;
        while ((readLength = in.read(buffer)) != -1) {
            output.append(new String(buffer, 0, readLength));
        }
        return output.toString();
    }

    /**
     * 发送用户名和密码
     *
     * @param username 用户名
     * @param password 密码
     * @throws IOException IO异常
     */
    public void sendCredentials(String username, String password) throws IOException {
        PrintStream out = new PrintStream(telnetClient.getOutputStream());
        out.println(username);
        out.flush();
        out.println(password);
        out.flush();
    }

    /**
     * 关闭Telnet连接
     */
    public void disconnect() {
        try {
            if (telnetClient.isConnected()) {
                telnetClient.disconnect();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}