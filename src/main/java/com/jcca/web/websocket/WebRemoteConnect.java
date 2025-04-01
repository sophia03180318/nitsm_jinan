package com.jcca.web.websocket;

import cn.hutool.json.JSONUtil;
import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.utils.AppLogUtils;
import com.jcca.web.websocket.util.SshRemoteUtil;
import com.jcca.web.websocket.util.TelnetRemoteUtil;
import com.jcraft.jsch.JSchException;
import org.apache.commons.net.telnet.TelnetClient;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: hhw
 * @description: WebRemoteConnect主要是用来
 * @date: 2025-03-31  10:03
 * @since: 2.1.4.0
 */
@Component
@ServerEndpoint("/ws/re/{username}")
public class WebRemoteConnect {


    private String username;

    private Map<String, Session> sessionPool = new ConcurrentHashMap<>();

    private Map<String, com.jcraft.jsch.Session> sshMap = new ConcurrentHashMap<>();
    private Map<String, TelnetClient> telnetMap = new ConcurrentHashMap<>();

    @OnOpen
    public synchronized void onOpen(Session session, @PathParam(value = "username") String username) {
        this.username = username;
        if (Objects.isNull(sessionPool.get(username))) {
            sessionPool.put(username, session);
            AppLogUtils.buildLogInfo(LogFunctionEnum.REAL_TIME_MSG, username, "准备远程访问设备");
        }
    }

    @OnClose
    public synchronized void onClose() {
        if (Objects.nonNull(sessionPool.get(username))) {
            sessionPool.remove(username);
            AppLogUtils.buildLogInfo(LogFunctionEnum.REAL_TIME_MSG, username, "已结束远程访问");
        }
        if (Objects.nonNull(sshMap.get(username))) {
            com.jcraft.jsch.Session remove = sshMap.remove(username);
            SshRemoteUtil.disconnect(remove);
            AppLogUtils.buildLogInfo(LogFunctionEnum.REAL_TIME_MSG, username, "已断开SSH远程连接");
        }
        if (Objects.nonNull(telnetMap.get(username))) {
            TelnetClient remove = telnetMap.remove(username);
            TelnetRemoteUtil.disconnect(remove);
            AppLogUtils.buildLogInfo(LogFunctionEnum.REAL_TIME_MSG, username, "已断开TELNET远程连接");
        }
    }

    @OnMessage
    public synchronized void onMessage(String message, Session session) {
        String path = session.getRequestURI().getPath();

        String username = path.substring(path.lastIndexOf("/") + 1);
        if (StringUtils.isEmpty(username) || "undefined".equals(username)) {
            return;
        }
        AppLogUtils.buildLogInfo(LogFunctionEnum.REAL_TIME_MSG, username, "收到远程登录消息：" + message);
        RemoteConnetDto dto = JSONUtil.toBean(message, RemoteConnetDto.class);
        if ("SSH".equals(dto.getMsgType())) {
            this.ssh(dto, session);
            return;
        }

        if ("TELNET".equals(dto.getMsgType())) {
            this.telnet(dto, session);
        }
    }

    private void telnet(RemoteConnetDto dto, Session session) {
        dto.setMessage("TELNET连接测试");
        TelnetClient connect = telnetMap.get(username);
        if (Objects.isNull(connect)) {
            String host = dto.getHost();
            String passwd = dto.getPasswd();
            if (StringUtils.isEmpty(host) || StringUtils.isEmpty(passwd)) {
                dto.setMessage("主机IP、密码均不能为空");
                session.getAsyncRemote().sendText(JSONUtil.toJsonStr(dto));
                return;
            }
            try {
                connect = TelnetRemoteUtil.connect(dto.getHost(), dto.getPort());
                telnetMap.put(username, connect);
            } catch (IOException e) {
                dto.setMessage(e.getMessage());
                session.getAsyncRemote().sendText(JSONUtil.toJsonStr(dto));
                telnetMap.remove(username);
                return;
            }
        }

        try {
            String s = TelnetRemoteUtil.executeCommand(connect, dto.getMessage());
            dto.setMessage(s);
            session.getAsyncRemote().sendText(JSONUtil.toJsonStr(dto));
        } catch (IOException e) {
            dto.setMessage(e.getMessage());
            session.getAsyncRemote().sendText(JSONUtil.toJsonStr(dto));
            telnetMap.remove(username);
        }

    }

    private void ssh(RemoteConnetDto dto, Session session) {
        com.jcraft.jsch.Session connect = sshMap.get(username);
        if (connect == null) {
            String host = dto.getHost();
            String username1 = dto.getUsername();
            String passwd = dto.getPasswd();
            if (StringUtils.isEmpty(host) || StringUtils.isEmpty(username1) || StringUtils.isEmpty(passwd)) {
                dto.setMessage("主机IP、用户名、密码均不能为空");
                session.getAsyncRemote().sendText(JSONUtil.toJsonStr(dto));
                return;
            }
            try {
                connect = SshRemoteUtil.connect(host, dto.getPort() == null ? 22 : dto.getPort(), username1, passwd);
                sshMap.put(username, connect);
            } catch (JSchException e) {
                dto.setMessage(e.getMessage());
                session.getAsyncRemote().sendText(JSONUtil.toJsonStr(dto));
                sshMap.remove(username);
                return;
            }
        }
        try {
            String s = SshRemoteUtil.executeCommand(connect, dto.getMessage());
            dto.setMessage(s);
            session.getAsyncRemote().sendText(JSONUtil.toJsonStr(dto));
        } catch (Exception e) {
            dto.setMessage(e.getMessage());
            session.getAsyncRemote().sendText(JSONUtil.toJsonStr(dto));
            sshMap.remove(username);
        }
    }

    @OnError
    public void onError(Throwable error) {
        sshMap.remove(username);
        telnetMap.remove(username);
        AppLogUtils.buildLogError(LogFunctionEnum.REAL_TIME_MSG, "远程连接发生错误", error);
    }
}
