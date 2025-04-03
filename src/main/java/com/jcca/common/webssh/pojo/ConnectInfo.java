package com.jcca.common.webssh.pojo;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSch;
import lombok.Data;
import org.springframework.web.socket.WebSocketSession;

/**
 * @Description: ssh连接信息
 * @Author: NoCortY
 * @Date: 2020/3/8
 */
@Data
public class ConnectInfo {
    private WebSocketSession webSocketSession;
    private JSch jSch;
    private Channel channel;
}
