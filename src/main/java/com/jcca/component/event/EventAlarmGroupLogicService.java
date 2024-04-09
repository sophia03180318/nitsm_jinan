package com.jcca.component.event;


import com.jcca.component.event.bean.EventAlarmGroupQueueReq;

/**
 * 事件告警组规则业务
 *
 * @author lyp
 */
public interface EventAlarmGroupLogicService {

    /**
     * 解析事件
     *
     * @param group
     */
    void parseGroup(EventAlarmGroupQueueReq group);

    /**
     * 执行事件告警
     *
     * @param groupStrList
     */
    void exeAlarm(String groupStrList);

}
