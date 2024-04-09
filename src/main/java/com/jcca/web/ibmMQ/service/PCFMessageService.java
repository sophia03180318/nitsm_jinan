package com.jcca.web.ibmMQ.service;

import com.ibm.mq.pcf.PCFMessage;
import com.jcca.web.ibmMQ.domain.Connection;

public interface PCFMessageService {
    void connect(Connection paramConnection) throws Exception;

    boolean isConnected(Connection paramConnection);

    void testConnection(Connection var1) throws Exception;

    boolean existsConnection(String var1);

    PCFMessage[] sendRequest(Connection paramConnection, PCFMessage paramPCFMessage, int paramInt, boolean paramBoolean) throws Exception;

    String getQueueManagerName(Connection paramConnection, boolean paramBoolean) throws Exception;
}
