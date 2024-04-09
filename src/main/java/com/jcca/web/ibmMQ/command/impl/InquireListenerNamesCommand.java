package com.jcca.web.ibmMQ.command.impl;

import com.ibm.mq.MQException;
import com.ibm.mq.pcf.PCFMessage;
import com.jcca.web.ibmMQ.annotation.Command;
import com.jcca.web.ibmMQ.domain.Connection;
import com.jcca.web.ibmMQ.domain.Monitor;
import com.jcca.web.ibmMQ.vo.MQObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/*    */

@Command("INQUIRE_LISTENER_NAMES")
public class InquireListenerNamesCommand extends InquireCommand {
    private static final Logger log = LoggerFactory.getLogger(InquireListenerNamesCommand.class);

    public Object execute(Map<String, Object> context) throws Exception {
        Connection connection = getConnection(context);
        String objectName = getObjectName(context);
        try {
            PCFMessage[] responses = sendRequest(connection, PCFMessageFactory.createInquireListenerNames(objectName));
            if (log.isDebugEnabled()) {
                log.debug("Inquire listener names with '{}' and got responses size: {}", objectName, Integer.valueOf(responses.length));
            }
            List<MQObject> objects = new ArrayList<MQObject>(responses.length);
            for (PCFMessage response : responses) {
                String name = response.getStringParameterValue(3554);
                objects.add(new MQObject(Monitor.Category.Listener.name(), name.trim(), Monitor.Category.Listener.name()));
                if (log.isDebugEnabled()) {
                    log.debug("Listener '{}' is added.", name);
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
