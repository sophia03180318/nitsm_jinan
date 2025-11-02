package com.jcca.dataProcessing.Entity;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 从兴告警处理
 *
 * @author sophia
 */
@Data
public class CongxingEntity extends CommonEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 事件ID
     */
    private String eventId;

    /**
     * 告警类型
     */
    private String alarmType;

    /**
     * 告警类型编码
     */
    private Integer alarmCode;

    /**
     * 告警对象名称
     */
    private String sourceObject;

    /**
     * 告警内容
     */
    private String alarmContent;

    /**
     * 告警状态，0报警发生，1报警恢复
     */
    private int alarmState;

    /**
     * 事件触发时间
     */
    private Date occurTime;

}
