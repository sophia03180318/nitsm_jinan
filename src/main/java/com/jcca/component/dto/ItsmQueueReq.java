package com.jcca.component.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * itsm 队列请求
 *
 * @author Lvyp
 */
@Data
public class ItsmQueueReq implements Serializable {

    private static final long serialVersionUID = 1L;


    /**
     * ITSM资产ID
     */
    private String assetId;

    /***
     实体Id
     */
    private Integer entityId;

    //ab 机标识
    private Integer abFlag;

    /**
     * 告警类型
     * AlarmTypeEnum
     */
    private String alarmType;

    /**
     * 属性索引
     */
    private Integer attrIndex;

    /**
     * 连接状态
     * LinkStatusEnum
     */
    private String linkStatus;

    /**
     * 连接告警会有对端设备ID
     */
    private String assetBId;

    /**
     * 基础值
     */
    private String baseValue;

    /**
     * 采集到的值
     */
    private String collectValue;

    /**
     * 主备
     * HostRunStatusEnum
     */
    private String hostType;

    /**
     * 旧主备
     * HostRunStatusEnum
     */
    private String oldHostType;

    /**
     * 推送告警时间 yyyy-MM-dd HH:mm:ss
     */
    private String occurTime;
    /**
     * 告警类型
     * ReceiveAlarmTypeEnum
     */
    private String cascoAlarmType;

    /**
     * 属性组ID
     */
    private Integer attrGroupId;

    /**
     * 旧版本
     */
    private String oldVersion;

    /**
     * 新版本
     */
    private String nowVersion;

    /**
     * 卡斯柯软件名称
     */
    private String cascoSoftName;
    /**
     * 通号
     * 进程状态 1：启动 2：停止 3：报警，其他无效
     */
    private int processState;
    /**
     * 通号
     * 进程名称
     */
    private String processName;
    /**
     * 通号
     * 报警内容
     */
    private String alarmContent;
    /**
     * 通号
     * 告警状态，0报警发生，1报警恢复
     */
    private int alarmState;
    /**
     * 通号
     * 连接标识串
     */
    private String idStr;


    /**
     * 从兴
     * 对象名称
     */
    private String sourceObject;

    /**
     * 从兴
     * 对象IP
     */
    private String sourceIp;


    /**
     * 从兴
     * 告警时间
     */
    private Date alarmTime;

    /**
     * 从兴
     * 告警时间
     */
    private String eventId;
}
