package com.jcca.web.ibmMQ.command.impl;


import com.ibm.mq.pcf.PCFMessage;
import com.jcca.web.ibmMQ.annotation.Command;
import com.jcca.web.ibmMQ.domain.Connection;
import com.jcca.web.ibmMQ.domain.QueueData;
import com.jcca.web.ibmMQ.util.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;


@Command("CREATE_QUEUE")
public class CreateQueueCommand extends InquireCommand {
    private static final Logger log = LoggerFactory.getLogger(CreateQueueCommand.class);


    public Object execute(Map<String, Object> context) throws Exception {
        Connection connection = getConnection(context);
        String queueName = getObjectName(context);
        QueueData.QueueType queueType = getObjectType(context, QueueData.QueueType.class);
        Assert.notNull(connection);
        Assert.notNullOrEmpty(queueName);
        Assert.notNull(queueType);
        if (log.isDebugEnabled()) {
            log.debug("Creating queue with name '{}' and type '{}'", queueName, queueType);
        }
        PCFMessage request = PCFMessageFactory.createQueue(queueName, queueType);
        sendRequest(connection, request);
        if (log.isDebugEnabled()) {
            log.debug("Queue with name '{}' and type '{}' is created.", queueName, queueType);
        }
        return null;
    }
}