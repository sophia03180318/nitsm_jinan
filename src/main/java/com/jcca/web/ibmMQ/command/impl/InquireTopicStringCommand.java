package com.jcca.web.ibmMQ.command.impl;


import com.ibm.mq.pcf.PCFMessage;
import com.jcca.web.ibmMQ.annotation.Command;
import com.jcca.web.ibmMQ.domain.Connection;
import com.jcca.web.ibmMQ.util.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@Command("INQUIRE_TOPIC_STRING")
public class InquireTopicStringCommand extends InquireDataCommand {
    private static final Logger log = LoggerFactory.getLogger(InquireTopicStringCommand.class);

    public Object execute(Map<String, Object> context) throws Exception {
        Connection connection = getConnection(context);
        String objectName = getObjectName(context);
        Assert.notNull(connection);
        Assert.notNull(objectName);
        PCFMessage request = PCFMessageFactory.createInquireTopic(objectName);
        PCFMessage[] responses = sendRequest(connection, request);
        if (log.isDebugEnabled()) {
            log.debug("Inquire topic string and got responses size: {}", Integer.valueOf(responses.length));
        }
        String topicString = responses[0].getStringParameterValue(2094);
        if (log.isDebugEnabled()) {
            log.debug("Got topicString '{}' for topic object '{}' in connection '{}'", new Object[]{topicString, objectName, connection
                    .getName()});
        }
        return topicString;
    }
}

