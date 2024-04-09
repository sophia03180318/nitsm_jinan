package com.jcca.web.alarm.service.data;

import lombok.Data;

import java.io.Serializable;

/**
 * 告警单条信息
 *
 * @author lyp
 */
@Data
public class ExportAlarmInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 告警标题，类型
     */
    private String alarmTitle;
    /**
     * 级别
     */
    private String alarmLevel;
    /**
     * 告警原始信息
     */
    private String alarmDescription;
    /**
     * 告警信息
     */
    private String alarmContent;
    /**
     * 解决方案
     */
    private String opinion;
    /**
     * 备注
     */
    private String remark;
    /**
     * 发生事件
     */
    private String occurTime;
}
