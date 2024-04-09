package com.jcca.web.ibmMQ.command.impl;


import com.ibm.mq.pcf.PCFMessage;
import com.jcca.web.ibmMQ.annotation.Command;
import com.jcca.web.ibmMQ.domain.Connection;
import com.jcca.web.ibmMQ.util.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;


@Command("CREATE_SUBSCRIPTION")
public class CreateSubscriptionCommand extends InquireCommand {
    private static final Logger log = LoggerFactory.getLogger(CreateSubscriptionCommand.class);

    public Object execute(Map<String, Object> context) throws Exception {
        Connection connection = getConnection(context);
        String subscriptionName = getSubscriptionName(context);
        String topicString = getSubscriptionTopicString(context);
        String destination = getSubscriptionDestination(context);
        Assert.notNull(connection);
        Assert.notNullOrEmpty(subscriptionName);
        Assert.notNullOrEmpty(topicString);
        Assert.notNullOrEmpty(destination);
        if (log.isDebugEnabled()) {
            log.debug("Creating a subscription with subscriptionName '{}', topicString '{}', destination '{}' in connection '{}'.", new Object[]{subscriptionName, topicString, destination, connection
                    .getName()});
        }
        PCFMessage request = PCFMessageFactory.createSubscription(subscriptionName, topicString, destination);
        sendRequest(connection, request);
        if (log.isDebugEnabled()) {
            log.debug("Subscription with subscriptionName '{}', topicString '{}', destination '{}' in in connection '{}' is created.", new Object[]{subscriptionName, topicString, destination, connection
                    .getName()});
        }
        return null;
    }
}

