package com.jcca.web.websocket;

import cn.hutool.json.JSONUtil;
import com.jcca.admin.system.entity.SysUser;
import com.jcca.admin.system.service.SysUserService;
import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.utils.AppLogUtils;
import com.jcca.common.utils.SpringContextUtil;
import com.jcca.web2.enums.DangerCommand;
import com.jcraft.jsch.JSchException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
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

    @OnOpen
    public synchronized void onOpen(Session session, @PathParam(value = "username") String username) {
        SysUserService userService = SpringContextUtil.getBean(SysUserService.class);
        SysUser user = userService.findByUsername(username);
        if (Objects.nonNull(user) && user.getUsername().equals(username)) {
            this.username = username;
            if (Objects.isNull(sessionPool.get(username))) {
                sessionPool.put(username, session);
                AppLogUtils.buildLogInfo(LogFunctionEnum.REAL_TIME_MSG, username,
                        "准备远程访问设备");
            }
        }
    }

    @OnClose
    public synchronized void onClose() {
        if (Objects.nonNull(sessionPool.get(username))) {
            sessionPool.remove(username);
            AppLogUtils.buildLogInfo(LogFunctionEnum.REAL_TIME_MSG, username,
                    "已结束远程访问");
        }
        if (Objects.nonNull(sshMap.get(username))) {
            com.jcraft.jsch.Session remove = sshMap.remove(username);
            SshRemoteUtil.disconnect(remove);
            AppLogUtils.buildLogInfo(LogFunctionEnum.REAL_TIME_MSG, username,
                    "已断开远程连接");
        }
    }

    @OnMessage
    public void onMessage(String message, Session session) {
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
        session.getAsyncRemote().sendText(JSONUtil.toJsonStr(dto));
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
                return;
            }
        }
        DangerCommand[] values = DangerCommand.values();
        for (DangerCommand value : values) {
            if (dto.getMessage().contains(value.getCommand())) {
                dto.setMessage("不允许的操作：" + dto.getMessage());
                session.getAsyncRemote().sendText(JSONUtil.toJsonStr(dto));
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
        }
    }

    @OnError
    public void onError(Throwable error) {
        AppLogUtils.buildLogError(LogFunctionEnum.REAL_TIME_MSG, "远程连接发生错误", error);
    }
}
