package com.jcca.component.event.bean;

import com.jcca.web.event.entity.AlarmEvent;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 事件告警队列
 *
 * @author lyp
 */
@Data
public class EventAlarmGroupQueueReq implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<AlarmEvent> newEventList;

}
