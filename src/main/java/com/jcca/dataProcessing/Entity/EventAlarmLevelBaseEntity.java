package com.jcca.dataProcessing.Entity;

import lombok.Data;

import java.util.Date;

/**
 * 模板字符串
 * @author Zhaozheng
 * @description TODO
 * @className EventAlarmLevelBaseEntity
 * @date 2023/11/20 9:39
 * @since 2.1.0.0
 */
@Data
public class EventAlarmLevelBaseEntity {

    /**
     * 规则库自己的ID
     */
    private String repoId;
    /**
     * 事件类型ID
     */
    private String EventTypeId;
    /**
     * 事件类型名称
     */
    private String eventTypeName;
    /**
     * 告警级别
     */
    private Integer alarmLevel;
    /**
     * 解决方案
     */
    private String opinion;
    /**
     * 关键字
     */
    private String statusFlag;
    /**
     * 状态匹配码的类型
     * EventLevelEnum
     */
    private Integer flagType;
    /**
     * 模板字符串
     */
    private String templateStr;
    /**
     * 创建时间
     */
    private Date createTime;



}
