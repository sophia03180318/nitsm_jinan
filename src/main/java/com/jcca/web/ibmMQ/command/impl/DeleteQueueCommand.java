package com.jcca.web.ibmMQ.command.impl;


import com.ibm.mq.pcf.PCFMessage;
import com.jcca.web.ibmMQ.annotation.Command;
import com.jcca.web.ibmMQ.domain.Connection;
import com.jcca.web.ibmMQ.util.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;


@Command("DELETE_QUEUE")
public class DeleteQueueCommand
        extends InquireCommand {
    private static final Logger log = LoggerFactory.getLogger(DeleteQueueCommand.class);


    public Object execute(Map<String, Object> context) throws Exception {
        Connection connection = getConnection(context);
        String queueName = getObjectName(context);
        Assert.notNullOrEmpty(queueName);
        Assert.notNull(connection);
        if (log.isDebugEnabled()) {
            log.debug("Deleting queue with name '{}'", queueName);
        }
        PCFMessage request = PCFMessageFactory.deleteQueue(queueName);
        sendRequest(connection, request);
        if (log.isDebugEnabled()) {
            log.debug("Queue with name '{}' is deleted.", queueName);
        }
        return null;
    }
}

