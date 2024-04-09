package com.jcca.component.quartz.warn.bean;

import com.jcca.web.alarm.entity.AlarmInfo;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @ Author：sophia
 * @ Date：Created in 15:57 2021/12/30
 * @ Description:
 */
@Data
public class LastAlarmFlag {


    /**
     * 用户ID
     */
    private String userId;

    /**
     * 用户最新的告警时间(入库时间)
     */
    private Date occurTime;

    /**
     * 用户对应的告警列表
     */
    private List<AlarmInfo> alarmInfos;

    public static LastAlarmFlag getLastAlarmFlag(String userId) {
        LastAlarmFlag lastAlarmFlag = new LastAlarmFlag();
        lastAlarmFlag.setUserId(userId);
        return lastAlarmFlag;
    }


}
