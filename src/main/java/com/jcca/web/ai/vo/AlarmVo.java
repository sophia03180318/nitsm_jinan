package com.jcca.web.ai.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 告警信息
 */
@Data
public class AlarmVo implements Serializable {

    private String title;

    private Byte alarmState;

    private String statusStr;

    private Date occurTime;

    private String description;

    @Override
    public String toString() {
        if (alarmState == 1) {
            statusStr = "未恢复";
        } else {
            statusStr = "已恢复";
        }

        return "{告警标题=" + title +
                ",告警状态'" + statusStr +
                ",发生时间'" + occurTime +
                ",告警详情'" + description +
                '}';
    }
}
