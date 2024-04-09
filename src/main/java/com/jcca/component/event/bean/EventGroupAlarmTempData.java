package com.jcca.component.event.bean;

import lombok.Data;

/**
 * 事件组告警可选模板参数
 *
 * @author lyp
 */
@Data
public class EventGroupAlarmTempData {

    /**
     * 资产组编号
     */
    private String assetGroupCode;
    /**
     * 资产所在位置
     */
    private String orgPosition;
    /**
     * 发生告警的资产IP
     */
    private String assetIp;
    /**
     * 发生告警的资产名称
     */
    private String assetName;
    /**
     * 告警级别
     */
    private String level;
    /**
     * 设定值
     */
    private String baseValue;
    /**
     * 采集值
     */
    private String collectValue;
    /**
     * 数据采集时间
     */
    private String collectTimeStr;
    /**
     * 特殊标记
     */
    private String eventFlag;
    /**
     * 原始内容
     */
    private String content;

    /**
     * 进程CPU使用率前5
     */
    private String processTop5CPU;
    /**
     * 进程内存使用率前5
     */
    private String processTop5Mem;

}
