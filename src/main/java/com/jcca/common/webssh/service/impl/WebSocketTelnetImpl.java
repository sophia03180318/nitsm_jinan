package com.jcca.common.webssh.service.impl;

import com.jcca.common.webssh.service.WebSocketTelnetService;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author: hhw
 * @description: WebSocketTelnetImpl主要是用来
 * @date: 2025-04-03  13:43
 * @since: 2.1.5.0
 */
@Service
public class WebSocketTelnetImpl implements WebSocketTelnetService {

    private static Map<String, Object> telnetMap = new ConcurrentHashMap<>();
    private ExecutorService executorService = Executors.newCachedThreadPool();

    @Override
    public void initConnection(WebSocketSession session) {
    }

    @Override
    public void recvHandle(String buffer, WebSocketSession session) {

    }

    @Override
    public void sendMessage(WebSocketSession session, byte[] buffer) throws IOException {

    }

    @Override
    public void close(WebSocketSession session) {

    }
}
