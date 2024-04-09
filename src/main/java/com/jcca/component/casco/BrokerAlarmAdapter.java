package com.jcca.component.casco;

import com.jcca.component.dto.ItsmQueueReq;

/**
 * 第三方业务告警处理接口
 * 包括：阈值告警，连接告警，主备切换，软件版本变化
 *
 * @author hanwone
 * @date 2021-01-07
 */
public interface BrokerAlarmAdapter {

    String getCode();

    void handle(ItsmQueueReq alarmDto);
}
