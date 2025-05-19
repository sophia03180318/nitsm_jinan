package com.jcca.common.webssh.websocket;

import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.utils.AppLogUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: hhw
 * @description: XunjianWebSocketHandler 主要是用来处理智能巡检实时状态消息
 * @date: 2025-05-18  20:29
 * @since: 2.1.6.0
 */
@Component
public class XunjianWebSocketHandler implements WebSocketHandler {

    public static Map<String, WebSocketSession> XUNJIAN_WEBSOCKET_MAP = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String path = Objects.requireNonNull(session.getUri()).getPath();
        String username = path.substring(path.lastIndexOf("/") + 1);
        AppLogUtils.buildLogInfo(LogFunctionEnum.REMOTE_CONNECT, "智能巡检连接成功", username);
        XUNJIAN_WEBSOCKET_MAP.put(username, session);
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        if (message instanceof TextMessage) {
            String payload = ((TextMessage) message).getPayload();

        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        AppLogUtils.buildLogInfo(LogFunctionEnum.REMOTE_CONNECT, "智能巡检数据传输错误", exception.getMessage());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        String path = Objects.requireNonNull(session.getUri()).getPath();
        String username = path.substring(path.lastIndexOf("/") + 1);
        AppLogUtils.buildLogInfo(LogFunctionEnum.REMOTE_CONNECT, "用户断开智能巡检连接", username);
        XUNJIAN_WEBSOCKET_MAP.remove(username);
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }
}
