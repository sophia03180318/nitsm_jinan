package com.jcca.common.webssh.websocket;

import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.utils.AppLogUtils;
import com.jcca.web2.dto.xunjian.XunjianWSDto;
import com.jcca.web2.service.XunjianScheduleService;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

import javax.annotation.Resource;
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

    @Resource
    private XunjianScheduleService xunjianScheduleService;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String path = Objects.requireNonNull(session.getUri()).getPath();
        String username = path.substring(path.lastIndexOf("/") + 1);
        AppLogUtils.buildLogInfo(LogFunctionEnum.XUNJIAN_MANAGE, "智能巡检连接成功", username);
        XUNJIAN_WEBSOCKET_MAP.put(username, session);
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        String path = Objects.requireNonNull(session.getUri()).getPath();
        String username = path.substring(path.lastIndexOf("/") + 1);
        XunjianWSDto sendMsg = new XunjianWSDto();
        sendMsg.setUsername(username);
        sendMsg.setMsgType(XunjianWSDto.HEART_BEAT);
        XunjianWSDto msg = new XunjianWSDto();
        msg.setName("OK");
        sendMsg.setMessage(msg);
        xunjianScheduleService.sendWsMsg(sendMsg);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        String path = Objects.requireNonNull(session.getUri()).getPath();
        String username = path.substring(path.lastIndexOf("/") + 1);
        XUNJIAN_WEBSOCKET_MAP.remove(username);
        AppLogUtils.buildLogInfo(LogFunctionEnum.XUNJIAN_MANAGE, "智能巡检数据传输错误", exception.getMessage());
        if (session.isOpen()) {
            session.close();
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        String path = Objects.requireNonNull(session.getUri()).getPath();
        String username = path.substring(path.lastIndexOf("/") + 1);
        AppLogUtils.buildLogInfo(LogFunctionEnum.XUNJIAN_MANAGE, "用户断开智能巡检连接", username);
        XUNJIAN_WEBSOCKET_MAP.remove(username);
        if (session.isOpen()) {
            session.close();
        }
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }
}
