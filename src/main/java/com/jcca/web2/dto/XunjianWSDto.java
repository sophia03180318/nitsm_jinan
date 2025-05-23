package com.jcca.web2.dto;

import lombok.Data;

/**
 * @author: hhw
 * @description: XunjianWSDto 主要是用来
 * @date: 2025-05-21  10:44
 * @since: 2.1.6.0
 */
@Data
public class XunjianWSDto {

    public static Integer HEART_BEAT = 1;
    public static Integer ASSET_STATUS = 2;
    public static Integer TARGET_STATUS = 3;
    public static Integer WHOLE_PROCESS = 4;
    public static Integer XUNJIANING_ASSET = 5;

    private String username;
    private Integer msgType;
    private XunjianWSDto message;

    private String jobId;
    private String id;
    private String name;
    private Integer status;
    private Integer count;
}
