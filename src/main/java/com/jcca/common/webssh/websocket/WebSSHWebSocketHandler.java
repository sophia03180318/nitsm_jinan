package com.jcca.common.webssh.websocket;

import cn.hutool.json.JSONUtil;
import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.utils.AppLogUtils;
import com.jcca.common.webssh.pojo.WebSSHData;
import com.jcca.common.webssh.service.WebSocketService;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

import javax.annotation.Resource;
import java.util.Objects;


/**
 * @Description: WebSSH的WebSocket处理器
 * @Author: NoCortY
 * @Date: 2020/3/8
 */
@Component
public class WebSSHWebSocketHandler implements WebSocketHandler {
    @Resource
    private WebSocketService webSocketService;

    /**
     * @Description: 用户连接上WebSocket的回调
     * @Param: [webSocketSession]
     * @return: void
     * @Author: NoCortY
     * @Date: 2020/3/8
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession webSocketSession) throws Exception {
        String path = Objects.requireNonNull(webSocketSession.getUri()).getPath();
        String username = path.substring(path.lastIndexOf("/") + 1);
        AppLogUtils.buildLogInfo(LogFunctionEnum.REMOTE_CONNECT, "准备远程访问设备", username);
        webSocketService.initConnection(webSocketSession);
    }

    /**
     * @Description: 收到消息的回调
     * @Param: [webSocketSession, webSocketMessage]
     * @return: void
     * @Author: NoCortY
     * @Date: 2020/3/8
     */
    @Override
    public void handleMessage(WebSocketSession webSocketSession, WebSocketMessage<?> webSocketMessage) throws Exception {
        if (webSocketMessage instanceof TextMessage) {
            AppLogUtils.buildLogInfo(LogFunctionEnum.REMOTE_CONNECT, "接收到用户命令", webSocketMessage);
            String payload = ((TextMessage) webSocketMessage).getPayload();
            WebSSHData data = JSONUtil.toBean(payload, WebSSHData.class);
            if ("SSH".equals(data.getMsgType())) {
                webSocketService.recvHandle(payload, webSocketSession);
                return;
            }
            if ("TELNET".equals(data.getMsgType())) {
                webSocketService.recvHandle(payload, webSocketSession);
            }
        }
    }

    /**
     * @Description: 出现错误的回调
     * @Param: [webSocketSession, throwable]
     * @return: void
     * @Author: NoCortY
     * @Date: 2020/3/8
     */
    @Override
    public void handleTransportError(WebSocketSession webSocketSession, Throwable throwable) throws Exception {
        String path = Objects.requireNonNull(webSocketSession.getUri()).getPath();
        String username = path.substring(path.lastIndexOf("/") + 1);
        AppLogUtils.buildLogInfo(LogFunctionEnum.REMOTE_CONNECT, "数据传输错误", username);
    }

    /**
     * @Description: 连接关闭的回调
     * @Param: [webSocketSession, closeStatus]
     * @return: void
     * @Author: NoCortY
     * @Date: 2020/3/8
     */
    @Override
    public void afterConnectionClosed(WebSocketSession webSocketSession, CloseStatus closeStatus) throws Exception {
        String path = Objects.requireNonNull(webSocketSession.getUri()).getPath();
        String username = path.substring(path.lastIndexOf("/") + 1);
        AppLogUtils.buildLogInfo(LogFunctionEnum.REMOTE_CONNECT, "用户断开远程访问连接", username);
        webSocketService.close(webSocketSession);
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }
}
