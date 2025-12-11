package com.jcca.web2.service.impl;


import cn.hutool.json.JSONUtil;
import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.utils.AppLogUtils;
import com.jcca.common.webssh.websocket.XunjianWebSocketHandler;
import com.jcca.web2.dto.xunjian.InspectBaseDataWsVo;
import com.jcca.web2.dto.xunjian.XunjianWSDto;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

/**
 * @author lifp
 * @version 1.0
 * @description: TODO
 * @date 2025-12-04 星期四 10:43:11
 */
@Service
public class XunJianSocketService {


    public void sendWsMsg(XunjianWSDto wsDto) {
        String operator = wsDto.getUsername();
        WebSocketSession webSocketSession = XunjianWebSocketHandler.XUNJIAN_WEBSOCKET_MAP.get(operator);
        if (webSocketSession == null || !webSocketSession.isOpen()) {
//            AppLogUtils.buildLogInfo(LogFunctionEnum.XUNJIAN_REALTIME, "用户WEBSOCKET连接失效", wsDto);
            return;
        }
        try {
            webSocketSession.sendMessage(new TextMessage(JSONUtil.toJsonStr(wsDto)));
            XunjianWSDto message = wsDto.getMessage();
            if ("100".equals(message.getId()) && message.getStatus() == 100) {
                AppLogUtils.buildLogInfo(LogFunctionEnum.XUNJIAN_MANAGE, "巡检采集给前端发送消息", wsDto);
            }
        } catch (IOException e) {
            AppLogUtils.buildLogError(LogFunctionEnum.XUNJIAN_MANAGE, "巡检采集给前端发送消息异常", wsDto);
        }
    }

    public void sendWsMsgV2(InspectBaseDataWsVo wsDto, String operator) {
        WebSocketSession webSocketSession = XunjianWebSocketHandler.XUNJIAN_WEBSOCKET_MAP.get(operator);
        if (webSocketSession == null || !webSocketSession.isOpen()) {
            return;
        }
        try {
            webSocketSession.sendMessage(new TextMessage(JSONUtil.toJsonStr(wsDto)));
        } catch (IOException e) {
            AppLogUtils.buildLogError(LogFunctionEnum.XUNJIAN_MANAGE, "巡检采集给前端发送消息异常", wsDto);
        }
    }
}
