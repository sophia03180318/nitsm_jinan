package com.jcca.component.other;

import com.jcca.component.dto.ReceiveAlarmDto;

/**
 * @ClassName AlarmAdapter
 * @Description 告警处理适配器
 * @Date 2020/6/22 14:25
 * @Author hanwone
 */
public interface AlarmAdapter {

    String getCode();

    void handle(ReceiveAlarmDto alarmDto);

}
