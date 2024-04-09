package com.jcca.web.ibmMQ.command.impl;


import com.ibm.mq.pcf.PCFException;
import com.ibm.mq.pcf.PCFMessage;
import com.jcca.web.ibmMQ.annotation.Command;
import com.jcca.web.ibmMQ.domain.Connection;
import com.jcca.web.ibmMQ.domain.MQTTChannelData;
import com.jcca.web.ibmMQ.util.Assert;
import com.jcca.web.ibmMQ.util.Metadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Command("INQUIRE_MQTT_CHANNEL")
public class InquireMQTTChannelCommand extends InquireDataCommand {
    private static final Logger log = LoggerFactory.getLogger(InquireMQTTChannelCommand.class);
    private PCFMessage channelStatusRequest;

    public Object execute(Map<String, Object> context) throws Exception {
        Connection connection = getConnection(context);
        Assert.notNull(connection);
        String channelName = getObjectName(context);
        if (this.channelStatusRequest == null) {
            this.channelStatusRequest = PCFMessageFactory.createInquireMQTTChannelStatus(channelName);
        }
        List<MQTTChannelData> dataList = new ArrayList<MQTTChannelData>();
        try {
            PCFMessage[] responses = sendRequest(connection, this.channelStatusRequest);
            if (log.isDebugEnabled()) {
                log.debug("Inquire MQTT channel status and got responses size: {}", Integer.valueOf(responses.length));
            }
            for (PCFMessage response : responses) {
                MQTTChannelData data = new MQTTChannelData();
                Metadata.MQTTChannel.invoke(data, response);
                if (log.isDebugEnabled()) {
                    log.debug("Got MQTT channel status: {}", data);
                }
                dataList.add(data);
            }
        } catch (PCFException e) {
            if (e.getReason() == 3065) {
                if (log.isDebugEnabled()) {
                    log.debug("MQTT Channel {} status not available!", (channelName == null) ? "" : channelName);
                }
            } else {
                throw e;
            }
        }
        return dataList;
    }
}