package com.jcca.component.event.bean;

import com.jcca.web.event.entity.AlarmEvent;
import lombok.Data;

import java.util.List;

/**
 * 创建过滤事件结果
 *
 * @author lyp
 */
@Data
public class CreateEventResult {

    /**
     * 需要保存的事件
     */
    private List<AlarmEvent> saveList;
    /**
     * 需要告警的事件列表
     */
    private List<AlarmEvent> alarmQueueList;

    private List<AlarmEvent> needNotifyList;

}
