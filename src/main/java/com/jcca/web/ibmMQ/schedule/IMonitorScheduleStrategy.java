package com.jcca.web.ibmMQ.schedule;


import com.jcca.web.ibmMQ.domain.Monitor;
import com.jcca.web.ibmMQ.support.INamedServiceProvider;

public interface IMonitorScheduleStrategy extends INamedServiceProvider {
    boolean canSchedule(Monitor paramMonitor);
}
