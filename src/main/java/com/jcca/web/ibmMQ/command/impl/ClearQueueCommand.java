package com.jcca.web.ibmMQ.command.impl;


import com.ibm.mq.pcf.PCFMessage;
import com.jcca.web.ibmMQ.annotation.Command;
import com.jcca.web.ibmMQ.domain.Connection;
import com.jcca.web.ibmMQ.util.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;


@Command("CLEAR_QUEUE")
public class ClearQueueCommand extends InquireCommand {
    private static final Logger log = LoggerFactory.getLogger(ClearQueueCommand.class);

    public Object execute(Map<String, Object> context) throws Exception {
        Connection connection = getConnection(context);
        String queueName = getObjectName(context);
        Assert.notNull(connection);
        Assert.notNullOrEmpty(queueName);
        if (log.isDebugEnabled()) {
            log.debug("Clearing queue '{}'...", queueName);
        }
        PCFMessage request = PCFMessageFactory.clearQueue(queueName);
        sendRequest(connection, request);
        if (log.isDebugEnabled()) {
            log.debug("Queue '{}' is cleared.", queueName);
        }
        return null;
    }
}