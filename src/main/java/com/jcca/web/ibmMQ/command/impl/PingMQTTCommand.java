package com.jcca.web.ibmMQ.command.impl;


import com.ibm.mq.MQException;
import com.ibm.mq.constants.MQConstants;
import com.ibm.mq.pcf.PCFMessage;
import com.jcca.web.ibmMQ.annotation.Command;
import com.jcca.web.ibmMQ.domain.Connection;
import com.jcca.web.ibmMQ.util.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;


@Command("PING_MQTT")
public class PingMQTTCommand extends InquireCommand {
    private static final Logger log = LoggerFactory.getLogger(PingMQTTCommand.class);
    private PCFMessage request;

    public Object execute(Map<String, Object> context) throws Exception {
        Connection connection = getConnection(context);
        Assert.notNull(connection);
        if (this.request == null) {
            this.request = PCFMessageFactory.createPingMQTTService();
        }
        try {
            PCFMessage[] responses = sendRequestWithoutCheck(connection, this.request, -1);
            if (responses.length <= 2 && responses[0].getCompCode() != 0) {
                if (log.isDebugEnabled()) {
                    log.debug("Reason Code: {}({}), MQTT MQXR Service is probably not defined and started!",
                            Integer.valueOf(responses[0].getReason()), MQConstants.lookupReasonCode(responses[0].getReason()));
                }
                return Boolean.valueOf(false);
            }
        } catch (Exception e) {
            if (e instanceof MQException) {
                MQException mqe = (MQException) e;
                if (log.isDebugEnabled()) {
                    log.debug("Reason Code: {}({}), MQTT MQXR Service unavailable!", Integer.valueOf(mqe.getReason()),
                            MQConstants.lookupReasonCode(mqe.getReason()));
                }
                return Boolean.valueOf(false);
            }
            throw e;
        }
        return Boolean.valueOf(true);
    }
}

