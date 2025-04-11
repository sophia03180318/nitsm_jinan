package com.jcca.common.webssh.service;

import com.jcca.common.webssh.pojo.WebRemoteData;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

/**
 * @author: hhw
 * @description: WebSocketTelnetService主要是用来
 * @date: 2025-04-03  14:07
 * @since: 2.1.5.0
 */
public interface WebSocketTelnetService {

    void initConnection(WebSocketSession session, String username);


    void recvHandle(String buffer, WebSocketSession session);


    void sendMessage(WebRemoteData webRemoteData, WebSocketSession session, byte[] buffer) throws IOException;


    void close(WebSocketSession session, String username);
}
