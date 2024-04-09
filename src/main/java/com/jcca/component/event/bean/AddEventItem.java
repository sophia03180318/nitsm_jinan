package com.jcca.component.event.bean;

import com.jcca.web.alarm.entity.AlarmRepository;
import com.jcca.web.event.entity.AlarmEventType;
import lombok.Data;

/**
 * 添加事件请求消息
 *
 * @author lyp
 */
@Data
public class AddEventItem {

    private AlarmEventType type;

    private AlarmRepository repo;


}
