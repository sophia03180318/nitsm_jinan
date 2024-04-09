package com.jcca.web.ibmMQ.command.impl;


import com.ibm.mq.pcf.PCFException;
import com.ibm.mq.pcf.PCFMessage;
import com.jcca.web.ibmMQ.annotation.Command;
import com.jcca.web.ibmMQ.domain.Monitor;
import com.jcca.web.ibmMQ.domain.QueueData;
import com.jcca.web.ibmMQ.domain.StatisticalData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Command("INQUIRE_Q")
public class InquireQCommand extends InquireDataCommand {
    private static final Logger log = LoggerFactory.getLogger(InquireQCommand.class);

    private PCFMessage inquireQRequest;
    private PCFMessage inquireQueueChannelsRequest;
    private int timeout;

    public Object execute(Map<String, Object> context) throws Exception {
        Monitor monitor = getMonitor(context);
        if (this.inquireQRequest == null) {
            this.inquireQRequest = PCFMessageFactory.createInquireQ(monitor.getObjectName());
            this.inquireQueueChannelsRequest = PCFMessageFactory.createInquireQChannels(monitor.getObjectName());
            this.timeout = getTimeout(monitor);
        }
        PCFMessage[] responses = sendRequest(monitor.getConnection(), this.inquireQRequest, this.timeout, isAutoConnect(context));
        if (log.isDebugEnabled()) {
            log.debug("Inquire Q responses size: {}", Integer.valueOf(responses.length));
        }
        List<StatisticalData> dataList = new ArrayList<StatisticalData>(1);
        StatisticalData data = handleResponse(monitor, responses[0], context);
        try {
            QueueData queueData = (QueueData) data.adapt(QueueData.class);
            if (Objects.nonNull(queueData.getCurrentQDepth())) {
                monitor.setValue(queueData.getCurrentQDepth());
            }
            PCFMessage[] statusResponses = sendRequest(monitor.getConnection(), this.inquireQueueChannelsRequest, this.timeout,
                    isAutoConnect(context));

            for (PCFMessage response : statusResponses) {
                String channelName = response.getStringParameterValue(3501).trim();
                if (!channelName.isEmpty()) {
                    queueData.getConnectedChannels().add(channelName);
                    if (log.isDebugEnabled()) {
                        log.debug("Channel '{}' is added", channelName);
                    }
                } else {
                    log.warn("The channel name of queue '{}' is empty of monitor '{}', just ignore it!", monitor
                            .getObjectName(), monitor.getName());
                }
            }
        } catch (PCFException e) {
            if (e.getReason() != 4091 && e.getReason() != 3200) {
                throw e;
            }
        }
        dataList.add(data);
        return dataList;
    }
}