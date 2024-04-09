package com.jcca.component.quartz.warn.bean;

import com.jcca.web.alarm.entity.AlarmInfo;
import lombok.Data;

import java.util.List;

/**
 * @ Author：sophia
 * @ Date：Created in 11:12 2021/12/30
 * @ Description:
 */
@Data
public class KeepAlarmVo {
    /**
     * ws类型
     * */
    private String wsType;


    /**
     * 告警条数
     */
    private Integer count;

    /**
     * 是否弹窗
     */
    private boolean popup;


    /**
     * 告警列表
     */
    private List<AlarmInfo> alarmInfoList;


}
