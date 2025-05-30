package com.jcca.web2.dto.xunjian;

import lombok.Data;

/**
 * @author: hhw
 * @description: XunjianWSDto 主要是用来
 * @date: 2025-05-21  10:44
 * @since: 2.1.6.0
 */
@Data
public class XunjianWSDto {

    /**
     * 心跳
     */
    public static Integer HEART_BEAT = 1;
    /**
     * 资产状态
     */
    public static Integer ASSET_STATUS = 2;
    /**
     * 指标状态
     */
    public static Integer TARGET_STATUS = 3;
    /**
     * 进度条
     */
    public static Integer WHOLE_PROCESS = 4;
    /**
     * 正在巡检设备
     */
    public static Integer XUNJIANING_ASSET = 5;
    /**
     * 指标状态实时统计
     */
    public static Integer TARGET_COUNT = 6;

    private String username;
    private Integer msgType;
    private XunjianWSDto message;

    private String inspectRecordId;
    private String jobId;
    private String id;
    private String name;
    private Integer status;
    private Integer count;
    private Integer normal;
    private Integer abnormal;
}
