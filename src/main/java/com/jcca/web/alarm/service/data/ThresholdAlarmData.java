package com.jcca.web.alarm.service.data;

import lombok.Data;

/**
 * @ClassName ThresholdAlarmData
 * @Description 接收阈值告警实体
 * @Date 2020/6/10 14:18
 * @Author hanwone
 */
@Data
public class ThresholdAlarmData {

    /**
     * 资产ID
     */
    private String assetId;
    /**
     * 采集信息种类，CPU,磁盘,内存。。。
     * ReceiveAlarmTypeEnum
     */
    private String category;
    /***
     * 告警类型
     * AlarmTypeEnum
     */
    private byte alarmType;
    /**
     * 告警匹配标识
     */
    private String alarmCode;
    /**
     * 设置的阈值
     */
    private String baseValue;
    /**
     * 采集到的当前值
     */
    private String collectValue;
    /**
     * 告警发生时间 yyyy-MM-dd HH:mm:ss
     */
    private String occurTime;
    /**
     * 备注
     */
    private String remark;
}
