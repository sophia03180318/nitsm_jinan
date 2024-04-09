package com.jcca.web.ibmMQ.command.impl;


import com.ibm.mq.MQException;
import com.ibm.mq.pcf.PCFMessage;
import com.jcca.web.ibmMQ.annotation.Command;
import com.jcca.web.ibmMQ.domain.Connection;
import com.jcca.web.ibmMQ.domain.Monitor;
import com.jcca.web.ibmMQ.domain.QueueData;
import com.jcca.web.ibmMQ.util.Metadata;
import com.jcca.web.ibmMQ.vo.MQObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;


@Command("INQUIRE_Q_NAMES")
public class InquireQNamesCommand
        extends InquireCommand {
    private static final Logger log = LoggerFactory.getLogger(InquireQNamesCommand.class);

    public Object execute(Map<String, Object> context) throws Exception {
        Connection connection = getConnection(context);
        String objectName = getObjectName(context);
        QueueData.QueueType type = getObjectType(context, QueueData.QueueType.class);
        try {
            PCFMessage[] responses = sendRequest(connection, PCFMessageFactory.createInquireQNames(objectName, type));
            if (log.isDebugEnabled()) {
                log.debug("Inquire Q names for queue {} with '{}' and got responses size: {}", new Object[]{(type == null) ? "all types" : type, objectName,
                        Integer.valueOf(responses.length)});
            }
            List<MQObject> objects = new ArrayList<MQObject>();
            String[] qNames = responses[0].getStringListParameterValue(3011);
            if (qNames != null && qNames.length > 0) {
                int[] qTypes = responses[0].getIntListParameterValue(1261);
                for (int i = 0; i < qNames.length; i++) {
                    String name = qNames[i].trim();
                    Enum<?> e = Metadata.Queue.getEnum(QueueData.QueueType.class, qTypes[i]);
                    objects.add(new MQObject(Monitor.Category.Queue.name(), name, e.name()));
                    if (log.isDebugEnabled()) {
                        log.debug("Queue '{}'  with type '{}' is added.", name, e);
                    }
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
