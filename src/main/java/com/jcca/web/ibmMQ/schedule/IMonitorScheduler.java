package com.jcca.web.ibmMQ.schedule;


import com.jcca.web.ibmMQ.domain.Monitor;

public interface IMonitorScheduler {

    //判断是否已经有任务
    boolean isMonitorScheduled(Monitor paramMonitor);

    //开启监视任务
    void scheduleMonitor(Monitor paramMonitor);

    //删除监视任务
    void unscheduleMonitor(Monitor paramMonitor);

    void rescheduleMonitor(Monitor paramMonitor);
}