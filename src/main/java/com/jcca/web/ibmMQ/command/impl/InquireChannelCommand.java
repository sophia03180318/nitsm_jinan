package com.jcca.web.ibmMQ.command.impl;


import com.ibm.mq.pcf.PCFException;
import com.ibm.mq.pcf.PCFMessage;
import com.jcca.web.ibmMQ.annotation.Command;
import com.jcca.web.ibmMQ.domain.ChannelData;
import com.jcca.web.ibmMQ.domain.Monitor;
import com.jcca.web.ibmMQ.domain.StatisticalData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Command("INQUIRE_CHANNEL")
public class InquireChannelCommand extends InquireDataCommand {
    private static final Logger log = LoggerFactory.getLogger(InquireChannelCommand.class);

    private static final String CHANNEL_STATUS_FIELD = "channelStatus";

    private PCFMessage channelStatusRequest;

    private PCFMessage channelRequest;
    private int timeout;


    public Object execute(Map<String, Object> context) throws Exception {
        Monitor monitor = getMonitor(context);
        if (this.channelStatusRequest == null) {
            this.channelStatusRequest = PCFMessageFactory.createInquireChannelStatus(monitor.getObjectName());
            this.channelRequest = PCFMessageFactory.createInquireChannel(monitor.getObjectName());
            this.timeout = getTimeout(monitor);
        }
        List<StatisticalData> dataList = new ArrayList<>();
        try {
            PCFMessage[] responses = sendRequest(monitor.getConnection(), this.channelStatusRequest, this.timeout,
                    isAutoConnect(context));
            if (log.isDebugEnabled()) {
                log.debug("Inquire channel status responses size: {}", Integer.valueOf(responses.length));
            }
            for (PCFMessage response : responses) {
                StatisticalData data = handleResponse(monitor, response, context);
                dataList.add(data);
            }
        } catch (PCFException e) {
            if (e.getReason() == 3065 || e.getReason() == 3200) {
                if (log.isDebugEnabled()) {
                    log.debug("Run MQCMD_INQUIRE_CHANNEL_STATUS failed with MQRCCF_CHL_STATUS_NOT_FOUND, means the channel is inactive, trying MQCMD_INQUIRE_CHANNEL to get data!");
                }
                PCFMessage[] responses = sendRequest(monitor.getConnection(), this.channelRequest, this.timeout,
                        isAutoConnect(context));
                if (log.isDebugEnabled()) {
                    log.debug("Inquire channel responses size: {}", Integer.valueOf(responses.length));
                }
                final ChannelData.ChannelStatus channelStatus = ChannelData.ChannelStatus.Inactive;
                this.setHealthEvaluationContext(context, new HashMap<String, Object>(1) {
                    {
                        this.put("channelStatus", channelStatus);
                    }
                });
                StatisticalData data = handleResponse(monitor, responses[0], context);
                ChannelData channelData = (ChannelData) data.adapt(ChannelData.class);
                channelData.setChannelStatus(channelStatus);
                dataList.add(data);
            } else {
                throw e;
            }
        }
        return dataList;
    }
}