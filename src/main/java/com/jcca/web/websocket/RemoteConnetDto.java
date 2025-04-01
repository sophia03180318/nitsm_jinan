package com.jcca.web.websocket;

import lombok.Data;

/**
 * @author: hhw
 * @description: RemoteConnetDto主要是用来处理websocket消息
 * @date: 2025-03-31  10:25
 * @since: 2.1.4.0
 */
@Data
public class RemoteConnetDto {

    // 用户名
    private String itsmUsername;
    // 消息类型 SSH TELNET
    private String msgType;
    // 消息内容
    private String message;

    private String host;
    private Integer port;
    private String username;
    private String passwd;
}
