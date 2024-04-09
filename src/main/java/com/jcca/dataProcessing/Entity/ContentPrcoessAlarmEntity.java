package com.jcca.dataProcessing.Entity;

import lombok.Data;

/**
 * 进程推告警content内数据
 *
 * @author Lvyp
 */
@Data
public class ContentPrcoessAlarmEntity extends CommonEntity {

    /**
     * 告警编号
     */
    private String alarmCode;
    /**
     * 进程名称
     */
    private String processName;

}
