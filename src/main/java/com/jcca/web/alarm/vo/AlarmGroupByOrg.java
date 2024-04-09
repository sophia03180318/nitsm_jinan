package com.jcca.web.alarm.vo;

import lombok.Data;

import java.util.Date;

/**
 * @ Author：sophia
 * @ Date：Created in 11:30 2021/12/30
 * @ Description:
 */
@Data
public class AlarmGroupByOrg {
    /**
     * 组织ID
     */
    private String orgId;

    /**
     * 组织名称
     */
    private String orgName;

    /**
     * 最新一条产生时间
     */
    private Date createTime;

    /**
     * 一级告警状态持续时间
     */
    private String keepAlarmTime;

    /**
     * 组织统计的告警条数
     */
    private Integer alarmNum;

}
