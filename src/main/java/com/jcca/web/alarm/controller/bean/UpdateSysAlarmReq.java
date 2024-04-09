package com.jcca.web.alarm.controller.bean;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 更新SYSlog告警
 *
 * @author lyp
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UpdateSysAlarmReq extends AddAlarmRepoReq {

    private static final long serialVersionUID = 1L;

    /**
     * 告警id
     */
    private String alarmInfoId;
}
