package com.jcca.web.ibmMQ.support;

import com.jcca.web.ibmMQ.domain.Monitor;

public interface IMonitorListener {

    void afterMonitorDeactivated(Monitor paramMonitor);

    void afterMonitorRemoved(Monitor paramMonitor);
}