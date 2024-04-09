package com.jcca.web.ibmMQ.command.impl;


import com.ibm.mq.MQException;
import com.ibm.mq.pcf.PCFMessage;
import com.jcca.web.ibmMQ.annotation.Command;
import com.jcca.web.ibmMQ.domain.ChannelData;
import com.jcca.web.ibmMQ.domain.Connection;
import com.jcca.web.ibmMQ.domain.Monitor;
import com.jcca.web.ibmMQ.util.Metadata;
import com.jcca.web.ibmMQ.vo.MQObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;


@Command("INQUIRE_CHANNEL_NAMES")
public class InquireChannelNamesCommand extends InquireCommand {
    private static final Logger log = LoggerFactory.getLogger(InquireChannelNamesCommand.class);


    public Object execute(Map<String, Object> context) throws Exception {
        Connection connection = getConnection(context);
        String objectName = getObjectName(context);
        ChannelData.ChannelType type = getObjectType(context, ChannelData.ChannelType.class);
        try {
            PCFMessage[] responses = sendRequest(connection, PCFMessageFactory.createInquireChannelNames(objectName, type));
            if (log.isDebugEnabled()) {
                log.debug("Inquire channel names for channel {} with '{}' and got responses size: {}", new Object[]{(type == null) ? "all types" : type, objectName,
                        Integer.valueOf(responses.length)});
            }
            List<MQObject> objects = new ArrayList<>();
            String[] channelNames = responses[0].getStringListParameterValue(3512);
            if (channelNames != null) {
                int[] channelTypes = (type == null) ? (int[]) responses[0].getParameterValue(1582) : null;
                for (int i = 0; i < channelNames.length; i++) {
                    String name = channelNames[i].trim();
                    int channelType = (type == null) ? channelTypes[i] : type.getValue();
                    Enum<?> e = Metadata.Channel.getEnum(ChannelData.ChannelType.class, channelType);
                    objects.add(new MQObject(Monitor.Category.Channel.name(), name, e.name()));
                    if (log.isDebugEnabled()) {
                        log.debug("Channel '{}' with type '{}' is added.", name, e);
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
