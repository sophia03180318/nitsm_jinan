package com.jcca.common.webssh.config;

import com.jcca.common.webssh.websocket.WebSSHWebSocketHandler;
import com.jcca.common.webssh.websocket.XunjianWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import javax.annotation.Resource;

/**
 * @Description: websocket配置
 * @Author: NoCortY
 * @Date: 2020/3/8
 */
@Configuration
@EnableWebSocket
public class WebSSHWebSocketConfig implements WebSocketConfigurer {
    @Autowired
    WebSSHWebSocketHandler webSSHWebSocketHandler;
    @Resource
    private XunjianWebSocketHandler xunjianWebSocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry webSocketHandlerRegistry) {
        webSocketHandlerRegistry
                .addHandler(webSSHWebSocketHandler, "/ws/re/{username}")
                .addHandler(xunjianWebSocketHandler, "/ws/xj/{username}")
                .setAllowedOrigins("*");
    }
}
