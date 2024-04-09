package com.jcca.web.ibmMQ.command.impl;


import com.ibm.mq.pcf.PCFMessage;
import com.jcca.web.ibmMQ.annotation.Command;
import com.jcca.web.ibmMQ.domain.Connection;
import com.jcca.web.ibmMQ.util.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;


@Command("DELETE_SUBSCRIPTION")
public class DeleteSubscriptionCommand extends InquireCommand {
    private static final Logger log = LoggerFactory.getLogger(DeleteSubscriptionCommand.class);

    public Object execute(Map<String, Object> context) throws Exception {
        Connection connection = getConnection(context);
        String subscriptionName = getSubscriptionName(context);
        Assert.notNull(connection);
        Assert.notNullOrEmpty(subscriptionName);
        if (log.isDebugEnabled()) {
            log.debug("Deleting subscription with name '{}' from connection '{}'", subscriptionName, connection);
        }
        PCFMessage request = PCFMessageFactory.deleteSubscription(subscriptionName);
        sendRequest(connection, request);
        if (log.isDebugEnabled()) {
            log.debug("Subscription with name '{}' is deleted from connection '{}'.", subscriptionName, connection);
        }
        return null;
    }
}