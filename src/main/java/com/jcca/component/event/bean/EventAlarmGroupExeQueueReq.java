package com.jcca.component.event.bean;

import com.jcca.web.event.entity.AlarmEvent;
import com.jcca.web.event.entity.AlarmEventGroup;
import lombok.Data;

import java.io.Serializable;

/**
 * 告警事件分发处理队列参数
 *
 * @author lyp
 */
@Data
public class EventAlarmGroupExeQueueReq implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * 触发此告警组的事件
     */
    private AlarmEvent alarmEvent;
    /**
     * 告警组规则
     */
    private AlarmEventGroup group;


}
