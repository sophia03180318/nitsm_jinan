package com.jcca.web.ibmMQ.notification;


import com.jcca.web.ibmMQ.domain.StatisticalData;

import java.util.List;

public interface INotificationService {
    List<INotificationProvider> getNotificationProviders();

    void notify(StatisticalData paramStatisticalData);
}

