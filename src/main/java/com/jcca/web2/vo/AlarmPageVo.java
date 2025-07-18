package com.jcca.web2.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;


/**
 * @description: 告警列表响应数据
 * @author: Lvyp
 * @create: 2023/11/22 09:25
 */
@Data
public class AlarmPageVo {

    /**
     * 组织类型
     */
    private Integer orgType;
    /**
     * 组织名称
     */
    private String orgName;
    /**
     * 资产ID
     */
    private String assetId;
    /**
     * 资产型号
     */
    private Integer assetMode;
    /**
     * 资产名称
     */
    private String assetName;
    /**
     * 资产IP1
     */
    private String assetIp1;
    /**
     * 资产IP2
     */
    private String assetIp2;
    /**
     * 告警ID
     */
    private String alarmId;
    /**
     * 告警所属类型
     * 1硬件告警2软件告警
     */
    private Integer alarmType;
    /**
     * 告警标题
     */
    private String title;
    /**
     * 告警级别
     */
    private Integer alarmLevel;
    /**
     * 告警内容
     */
    private String content;
    /**
     * 原始信息
     */
    private String description;
    /**
     * 告警标识
     */
    private String alarmCode;
    /**
     * 产生时间
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date occurTime;
    /**
     * 确认时间
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date confirmTime;
    /**
     * 确认人
     */
    private String confirmor;
    /**
     * 是否已经转成故障记录
     */
    private Integer alarmToRecord;
    /**
     * 解决方案
     */
    private String opinion;
    /**
     * 备注
     */
    private String remark;
    /**
     * 是否天窗告警
     * 1不是2是
     */
    private Integer blank;
    /**
     * 状态
     * 1未确认2已确认
     */
    private Integer status;
    /**
     * 告警状态
     * 1告警2恢复
     */
    private Integer alarmState;
    /**
     * 知识库名称
     */
    private  String repoName;
    /**
     * 追踪状态
     */
    private Integer traceStatus;

}
