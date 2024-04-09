package com.jcca.web2.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * @description: 机柜内告警信息
 * @author: Lvyp
 * @create: 2023/11/01 14:38
 */
@Data
public class CabinetAlarmInfoVo {
    /**
     * 告警ID
     */
    private String alarmInfoId;
    /**
     * 告警标题
     */
    private String title;
    /**
     * 告警状态
     * 1告警 2恢复
     */
    private Integer alarmState;
    /**
     * 确认状态
     * 1未确认 2已确认
     */
    private Integer status;
    /**
     * 告警内容
     */
    private String content;
    /**
     * 原始信息
     */
    private String description;
    /**
     * 发生时间
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date occurTime;
    /**
     * 资产ID
     */
    private String assetId;
    /**
     * 资产名称
     */
    private String assetName;
    /**
     * IP1
     */
    private String ip1;
    /**
     * IP2
     */
    private String ip2;

}
