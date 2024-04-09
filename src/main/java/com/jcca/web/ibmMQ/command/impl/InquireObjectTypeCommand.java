package com.jcca.web.ibmMQ.command.impl;


import com.ibm.mq.pcf.PCFMessage;
import com.jcca.web.ibmMQ.annotation.Command;
import com.jcca.web.ibmMQ.domain.*;
import com.jcca.web.ibmMQ.util.Metadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@Command("INQUIRE_OBJECT_TYPE")
public class InquireObjectTypeCommand extends InquireCommand {
    private static final Logger log = LoggerFactory.getLogger(InquireObjectTypeCommand.class);


    public Object execute(Map<String, Object> context) throws Exception {
        Connection connection = getConnection(context);
        String objectName = getObjectName(context);
        Monitor.Category category = getObjectCategory(context);
        if (category == Monitor.Category.QueueManager) {
            return Metadata.QueueManager;
        }
        PCFMessage[] responses = sendRequest(connection, PCFMessageFactory.createInquireObjectName(objectName, category));
        if (log.isDebugEnabled()) {
            log.debug("Inquire object type got responses size: {}", Integer.valueOf(responses.length));
        }
        PCFMessage response = responses[0];
        switch (category) {
            case Queue:
                return Metadata.Queue.getEnum(QueueData.QueueType.class, response.getIntParameterValue(20));
            case Topic:
                return Metadata.Topic.getEnum(TopicData.TopicType.class, response.getIntParameterValue(208));
            case Channel:
                return Metadata.Channel.getEnum(ChannelData.ChannelType.class, response.getIntParameterValue(1511));
            case Listener:
                return Metadata.Listener;
        }
        return null;
    }
}