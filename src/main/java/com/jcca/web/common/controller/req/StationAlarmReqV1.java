package com.jcca.web.common.controller.req;

import lombok.Data;

import java.util.Date;

/**
 * 车站告警信息
 * @author lyp
 */
@Data
public class StationAlarmReqV1 {

    /**
     * 车站IPI
     */
    private String stationIp;
    /**
     * 可能是空
     * 如果是空的话那这个告警就代表的是车站自己的告警
     * 如果不是空的话这个告警就是具体设备的告警
     */
    private String assetId;
    /**
     * 事件匹配码
     */
    private String uniqueCode;
    /**
     * 事件状态
     */
    private Integer eventLevel;
    /**
     * 阈值类 设定基础参数
     */
    private String baseValue;
    /**
     * 阈值类 采集到的参数
     */
    private String collectValue;
    /**
     * 原本的信息
     */
    private String originalMsg;
    /**
     * 特殊标记
     */
    private String flag;
    /**
     * 产生时间
     */
    private String createTime;
    /**
     * 匹配标识 如果事件之间的flag一样则告警需要验证alarmCode，否则的话不需要
     */
    private String groupFlag;


}
