package com.jcca.web.ibmMQ.notification;


import com.jcca.web.ibmMQ.domain.StatisticalData;
import com.jcca.web.ibmMQ.support.INamedServiceProvider;

public interface INotificationProvider extends INamedServiceProvider {
    void notify(StatisticalData paramStatisticalData);

    void shutdown();
}
