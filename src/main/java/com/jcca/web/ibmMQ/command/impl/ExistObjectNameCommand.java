package com.jcca.web.ibmMQ.command.impl;


import com.ibm.mq.pcf.PCFException;
import com.jcca.web.ibmMQ.annotation.Command;
import com.jcca.web.ibmMQ.domain.Connection;
import com.jcca.web.ibmMQ.domain.Monitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;


@Command("EXIST_OBJECT_NAME")
public class ExistObjectNameCommand extends InquireCommand {
    private static final Logger log = LoggerFactory.getLogger(ExistObjectNameCommand.class);


    public Object execute(Map<String, Object> context) throws Exception {
        Connection connection = getConnection(context);
        String objectName = getObjectName(context);
        Monitor.Category category = getObjectCategory(context);
        try {
            sendRequest(connection, PCFMessageFactory.createInquireObjectName(objectName, category));
            if (log.isDebugEnabled()) {
                log.debug("MQ object {} '{}' exists!", category, objectName);
            }
        } catch (PCFException e) {
            if (e.getReason() == 2085 || e.getReason() == 3200) {
                if (log.isDebugEnabled()) {
                    log.debug("MQ object {} '{}' not exists!", category, objectName);
                }
                return Boolean.valueOf(false);
            }
            throw e;
        }
        return Boolean.valueOf(true);
    }
}

