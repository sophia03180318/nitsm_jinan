package com.jcca.web.websocket;

import cn.hutool.core.util.StrUtil;
import com.jcca.admin.system.entity.SysUser;
import com.jcca.admin.system.service.SysUserService;
import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.utils.AppLogUtils;
import com.jcca.common.utils.SpringContextUtil;
import lombok.EqualsAndHashCode;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 告警消息推送
 *
 * @author hanwone
 * @Date 2020/06/02 13:31
 */
@Component
@ServerEndpoint(value = "/ws/{username}")
@EqualsAndHashCode
public class WebSocketServer {

    private String username;
    private Session session;

    public static Map<String, Session> sessionPool = new HashMap<>();

    @OnOpen
    public synchronized void onOpen(Session session, @PathParam(value = "username") String username) {
        SysUserService userService = SpringContextUtil.getBean(SysUserService.class);
        SysUser user = userService.findByUsername(username);
        if (Objects.nonNull(user)) {
            this.session = session;
            this.username = username;

            if (Objects.isNull(sessionPool.get(username))) {
                sessionPool.put(username, session);
                AppLogUtils.buildLogInfo(LogFunctionEnum.REAL_TIME_MSG, username,
                        "新连接加入，当前在线人数：" + sessionPool.keySet().size());
            }

        }
    }

    @OnClose
    public synchronized void onClose() {
        if (Objects.nonNull(sessionPool.get(username))) {
            sessionPool.remove(username);

            AppLogUtils.buildLogInfo(LogFunctionEnum.REAL_TIME_MSG, username,
                    "有连接关闭，当前在线人数：" + sessionPool.keySet().size());
        }
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        String path = session.getRequestURI().getPath();
        String username = path.substring(path.lastIndexOf("/") + 1);
        if ("undefined".equals(username)) {
            sessionPool.remove(username);
            return;
        }
        AppLogUtils.buildLogInfo(LogFunctionEnum.REAL_TIME_MSG, username, "收到心跳消息成功：" + message);
        if ("HEART_BEAT".equals(message)) {
            session.getAsyncRemote().sendText("OK");
        }
    }

    @OnError
    public void onError(Throwable error) {
        AppLogUtils.buildLogError(LogFunctionEnum.REAL_TIME_MSG, "websocket连接发生错误", error);
    }

    /**
     * 发对应在线人员
     *
     * @param userName
     * @param message
     */
    public static void sendMessage(String userName, String message) {
        try {
            Session session = sessionPool.get(userName);
            if (session != null && StrUtil.isNotEmpty(message)) {
                session.getBasicRemote().sendText(message);
            }
        } catch (Exception e) {
            AppLogUtils.buildLogError(LogFunctionEnum.REAL_TIME_MSG, userName + "：" + message, e);
        }
    }
}