package com.jcca.component.event.bean;

import com.jcca.web.event.entity.AlarmEvent;
import lombok.Data;

import java.util.List;

/**
 * 获取事件结果
 *
 * @author lyp
 */
@Data
public class EventGetGroupResult {

    /**
     * 是否需要匹配告警标识
     */
    private Boolean needMatchAlarm;
    /**
     * 结果 是否可以上告警
     */
    private Boolean result;
    /**
     * 需要关联的事件ID列表
     */
    private List<AlarmEvent> correlationEvent;

}
