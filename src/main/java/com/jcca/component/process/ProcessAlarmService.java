package com.jcca.component.process;

import com.jcca.component.process.bean.ProcessAlarmQueueBean;

import java.util.Date;
import java.util.List;

/**
 * 进程告警处理
 *
 * @author lyp
 */
public interface ProcessAlarmService {

    /**
     * 处理进程组告警
     *
     * @param queueObj
     */
    void disposeGroupAlarm(List<ProcessAlarmQueueBean> queueObj, Date time);

    /**
     * 处理进程告警
     *
     * @param queueObj
     * @param time
     */
    void disposeNoGroupAlarm(ProcessAlarmQueueBean queueObj, Date time);


}
