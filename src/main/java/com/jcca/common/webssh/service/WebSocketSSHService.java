package com.jcca.common.webssh.service;

import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

/**
 * @Description: WebSSH的业务逻辑
 * @Author: NoCortY
 * @Date: 2020/3/7
 */
public interface WebSocketSSHService {
    /**
     * @Description: 初始化ssh连接
     * @Param:
     * @return:
     * @Author: NoCortY
     * @Date: 2020/3/7
     */
    void initConnection(WebSocketSession session, String username);

    /**
     * @Description: 处理客户段发的数据
     * @Param:
     * @return:
     * @Author: NoCortY
     * @Date: 2020/3/7
     */
    void recvHandle(String buffer, WebSocketSession session);

    /**
     * @Description: 数据写回前端 for websocket
     * @Param:
     * @return:
     * @Author: NoCortY
     * @Date: 2020/3/7
     */
    void sendMessage(WebSocketSession session, byte[] buffer) throws IOException;

    /**
     * @Description: 关闭连接
     * @Param:
     * @return:
     * @Author: NoCortY
     * @Date: 2020/3/7
     */
    void close(WebSocketSession session, String username);
}
