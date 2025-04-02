package com.jcca.web.websocket;

import cn.hutool.json.JSONUtil;
import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.utils.AppLogUtils;
import com.jcca.web.websocket.util.SshRemoteUtil;
import com.jcca.web.websocket.util.TelnetRemoteUtil;
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
@ServerEndpoint("/ws/re")
public class WebRemoteConnect {

    private String username;

    public static Map<String, Session> SESSION_POOL = new ConcurrentHashMap<>();

    public Map<String, com.jcraft.jsch.Session> SSH_MAP = new ConcurrentHashMap<>();
    public Map<String, TelnetClient> TELNET_MAP = new ConcurrentHashMap<>();

    @OnOpen
    public synchronized void onOpen(Session session, @PathParam(value = "username") String username) {
        this.username = username;
        if (Objects.isNull(SESSION_POOL.get(username))) {
            SESSION_POOL.put(username, session);
            AppLogUtils.buildLogInfo(LogFunctionEnum.REMOTE_CONNECT, username, "准备远程访问设备");
        }
    }

    @OnClose
    public synchronized void onClose() {
        if (Objects.nonNull(SESSION_POOL.get(username))) {
            SESSION_POOL.remove(username);
            AppLogUtils.buildLogInfo(LogFunctionEnum.REMOTE_CONNECT, username, "已结束远程访问");
        }
        if (Objects.nonNull(SSH_MAP.get(username))) {
            com.jcraft.jsch.Session remove = SSH_MAP.remove(username);
            SshRemoteUtil.disconnect(remove);
            AppLogUtils.buildLogInfo(LogFunctionEnum.REMOTE_CONNECT, username, "已断开SSH远程连接");
        }
        if (Objects.nonNull(TELNET_MAP.get(username))) {
            TelnetClient remove = TELNET_MAP.remove(username);
            TelnetRemoteUtil.disconnect(remove);
            AppLogUtils.buildLogInfo(LogFunctionEnum.REMOTE_CONNECT, username, "已断开TELNET远程连接");
        }
    }

    @OnMessage
    public synchronized void onMessage(String message, Session session) {
        AppLogUtils.buildLogInfo(LogFunctionEnum.REMOTE_CONNECT, username, "收到远程登录消息：" + message);
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
        TelnetClient connect = TELNET_MAP.get(username);
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
                TELNET_MAP.put(username, connect);
            } catch (IOException e) {
                dto.setMessage(e.getMessage());
                session.getAsyncRemote().sendText(JSONUtil.toJsonStr(dto));
                TELNET_MAP.remove(username);
                return;
            }
        }

        dto.setMessage(dto.getMessage().replaceAll("\r", ""));
        if (StringUtils.isEmpty(dto.getMessage())) {
            return;
        }

        try {
            TelnetRemoteUtil.executeCommand(dto.getItsmUsername(), connect, dto.getMessage());
        } catch (IOException e) {
            dto.setMessage(e.getMessage());
            session.getAsyncRemote().sendText(JSONUtil.toJsonStr(dto));
            TELNET_MAP.remove(username);
        }

    }

    private void ssh(RemoteConnetDto dto, Session session) {
        com.jcraft.jsch.Session connect = SSH_MAP.get(username);
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
                SshRemoteUtil.shellConnect(dto, connect);
                SSH_MAP.put(username, connect);
            } catch (Exception e) {
                dto.setMessage(e.getMessage());
                session.getAsyncRemote().sendText(JSONUtil.toJsonStr(dto));
                SSH_MAP.remove(username);
                SshRemoteUtil.disconnect(connect);
            }
            return;
        }

        if (StringUtils.isEmpty(dto.getMessage())) {
            return;
        }
        dto.setMessage(dto.getMessage().replaceAll("\r", ""));
        if (StringUtils.isEmpty(dto.getMessage())) {
            return;
        }

        try {
            SshRemoteUtil.execCommand(dto.getItsmUsername(), dto.getMessage());
        } catch (Exception e) {
            dto.setMessage(e.getMessage());
            session.getAsyncRemote().sendText(JSONUtil.toJsonStr(dto));
            SSH_MAP.remove(username);
            SshRemoteUtil.disconnect(connect);
        }
    }

    @OnError
    public void onError(Throwable error) {
        com.jcraft.jsch.Session connect = SSH_MAP.remove(username);
        SshRemoteUtil.disconnect(connect);
        TELNET_MAP.remove(username);
        AppLogUtils.buildLogError(LogFunctionEnum.REMOTE_CONNECT, "远程连接发生错误", error);
    }
}
