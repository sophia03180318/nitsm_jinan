package com.jcca.component.thresholds.bean;

import lombok.Data;

/**
 * 进程推告警content内数据
 *
 * @author Lvyp
 */
@Data
public class ContentPrcoessAlarm {

    /**
     * 告警编号
     */
    private String alarmCode;
    /**
     * 进程名称
     */
    private String processName;

    public ContentPrcoessAlarm(String alarmCode, String processName) {
        super();
        this.alarmCode = alarmCode;
        this.processName = processName;
    }

    public ContentPrcoessAlarm() {
        super();
    }

}
