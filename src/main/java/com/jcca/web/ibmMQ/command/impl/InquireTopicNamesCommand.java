package com.jcca.web.ibmMQ.command.impl;


import com.ibm.mq.MQException;
import com.ibm.mq.pcf.PCFMessage;
import com.jcca.web.ibmMQ.annotation.Command;
import com.jcca.web.ibmMQ.domain.Connection;
import com.jcca.web.ibmMQ.domain.Monitor;
import com.jcca.web.ibmMQ.domain.TopicData;
import com.jcca.web.ibmMQ.util.Metadata;
import com.jcca.web.ibmMQ.vo.MQObject;
import com.jcca.web.ibmMQ.vo.TopicMQObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Command("INQUIRE_TOPIC_NAMES")
public class InquireTopicNamesCommand
        extends InquireCommand {
    private static final Logger log = LoggerFactory.getLogger(InquireTopicNamesCommand.class);

    public Object execute(Map<String, Object> context) throws Exception {
        Connection connection = getConnection(context);
        String objectName = getObjectName(context);
        TopicData.TopicType type = getObjectType(context, TopicData.TopicType.class);
        try {
            PCFMessage[] responses = sendRequest(connection, PCFMessageFactory.createInquireTopicNames(objectName, type));
            if (log.isDebugEnabled()) {

                log.debug("Inquire Topic names for topic {} with '{}' and got responses size: {}", new Object[]{(type == null) ? "all types" : type, objectName,
                        Integer.valueOf(responses.length)});
            }
            List<MQObject> objects = new ArrayList<MQObject>(responses.length);
            for (PCFMessage response : responses) {
                String name = response.getStringParameterValue(2092);
                int topicType = response.getIntParameterValue(208);
                String topicString = response.getStringParameterValue(2094);
                Enum<?> e = Metadata.Topic.getEnum(TopicData.TopicType.class, topicType);
                objects.add(new TopicMQObject(Monitor.Category.Topic.name(), name.trim(), e.name(), topicString));
                if (log.isDebugEnabled()) {
                    log.debug("Topic '{}' with type '{}' is added", name, e);
                }
            }
            return objects;
        } catch (MQException e) {
            if (isInquireObjectNamesFailed(e.getReason())) {
                return Collections.emptyList();
            }
            throw e;
        }
    }
}

