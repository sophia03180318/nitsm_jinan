package com.jcca.component.constants;

/**
 * @ClassName RedisQueueConst
 * @Description 采集器推送队列常量
 * @Date 2020/6/5 17:35
 * @Author hanwone
 */
public interface RedisQueueConst {

    /**
     * 非阈值队列
     */
    String ALARM_QUEUE = "_alarm_queue";

    /**
     * 阈值队列
     */
    String THRESHOLD_QUEUE = "_threshold_queue";

    /**
     * 前端告警推送队列
     */
    String ALARM_PUSH_FRONT_QUEUE = "_alarm_push_front_queue";

    // 统一业务告警队列
    String BROKER_QUEUE_KEY = "_broker_alarm_queue";

    /**
     * 事件告警处理器分发队列
     */
    String EVENT_GROUP_ALARM = "_event_alarm_group";
    /**
     * 事件告警处理器队列
     */
    String EVENT_GROUP_ALARM_EXE = "_event_alarm_group_exe:";
    /**
     * 事件添加队列
     */
    String EVENT_GROUP_ALARM_ADD = "_event_alarm_group_add:";

    /**
     * 智能巡检数据队列
     */
    String XUNJIAN_QUEUE = "_xunjian_queue";
}
