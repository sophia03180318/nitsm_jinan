package com.jcca.common.webssh.pojo;

import lombok.Data;

/**
 * @Description: webssh数据传输
 * @Author: NoCortY
 * @Date: 2020/3/8
 */
@Data
public class WebSSHData {
    private String operate;
    private String itsmUsername;
    // 消息类型 SSH TELNET
    private String msgType;
    private String host;
    private Integer port = 22;
    private String username;
    private String passwd;
    private String message = "";

    private int cols = 80;
    private int rows = 24;
    private int width = 640;
    private int height = 480;

}
