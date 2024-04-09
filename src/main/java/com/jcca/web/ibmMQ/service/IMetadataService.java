package com.jcca.web.ibmMQ.service;


import com.jcca.web.ibmMQ.domain.*;
import com.jcca.web.ibmMQ.vo.MQObject;

import java.util.List;

public interface IMetadataService {
    boolean isQMgrAvailable(Connection paramConnection);

    int pingQMgr(Connection paramConnection);

    boolean existObjectName(Connection paramConnection, Monitor.Category paramCategory, String paramString);

    String queryQMgrName(Connection paramConnection);

    List<MQObject> queryListenerNames(Connection paramConnection, String paramString);

    List<MQObject> queryChannelNames(Connection paramConnection, String paramString1, String paramString2);

    List<MQObject> queryQueueNames(Connection paramConnection, String paramString1, String paramString2);

    List<MQObject> queryTopicNames(Connection paramConnection, String paramString1, String paramString2);

    List<String> queryCategories();

    List<String> queryObjectTypes(String paramString);

    String queryObjectType(Connection paramConnection, String paramString1, String paramString2);

    List<String> queryMeasurements(String paramString1, String paramString2);

    StatisticalData queryMonitorDetails(Monitor paramMonitor);

    boolean isMQTTServiceAvailable(Connection paramConnection);

    List<MQTTChannelData> queryMQTTChannelStatistics(Connection paramConnection);

    void clearQueue(Connection paramConnection, String paramString);

    String querySubscriptionName(Monitor paramMonitor);

    String queryTopicString(Connection var1, String var2);

    String querySubscriptionDestination(Connection var1);

    void createSubscription(Connection var1, String var2, String var3, String var4);

    void deleteQueue(Connection paramConnection, String paramString);

    void createQueue(Connection paramConnection, String paramString, QueueData.QueueType paramQueueType);

    void deleteSubscription(Connection connection, String subscriptionName);


}