package com.jcca.web.ibmMQ.command.impl;


import com.ibm.mq.pcf.PCFMessage;
import com.jcca.web.ibmMQ.annotation.Command;
import com.jcca.web.ibmMQ.domain.Monitor;
import com.jcca.web.ibmMQ.domain.StatisticalData;
import com.jcca.web.ibmMQ.domain.TopicData;
import com.jcca.web.ibmMQ.util.Metadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Command("INQUIRE_TOPIC")
public class InquireTopicCommand
        extends InquireDataCommand {
    private static final Logger log = LoggerFactory.getLogger(InquireTopicCommand.class);

    private static final String TOPIC_PUB_MSG_COUNT_FIELD = "publishedMessagesCount";

    private PCFMessage inquireTopicRequest;

    private PCFMessage inquireTopicStatusRequest;

    private PCFMessage inquireSubStatusRequest;
    private int timeout;

    public Object execute(Map<String, Object> context) throws Exception {
        Monitor monitor = getMonitor(context);
        boolean autoConnect = isAutoConnect(context);
        if (this.inquireTopicStatusRequest == null) {
            this.timeout = getTimeout(monitor);
            String topicString = null;
            if (monitor.isMQTT()) {
                topicString = monitor.getObjectName();
            } else {
                this.inquireTopicRequest = PCFMessageFactory.createInquireTopic(monitor.getObjectName());
                TopicData topicData = getTopicData(null, monitor, null);
                topicString = topicData.getTopicString();
            }
            if (log.isDebugEnabled()) {
                log.debug("Got topicString '{}' for monitor '{}'", topicString, monitor.getName());
            }
            this.inquireTopicStatusRequest = PCFMessageFactory.createInquireTopicStatus(topicString, 1295);
            String subscriptionName = this.metadataService.querySubscriptionName(monitor);
            if (log.isDebugEnabled()) {
                log.debug("The subscriptionName for monitor: {} is '{}'", monitor.getName(), subscriptionName);
            }
            this.inquireSubStatusRequest = PCFMessageFactory.createInquireSubscriptionStatus(subscriptionName);
        }
        List<StatisticalData> dataList = new ArrayList<StatisticalData>();
        PCFMessage[] responses = sendRequest(monitor.getConnection(), this.inquireTopicStatusRequest, this.timeout, autoConnect);
        if (log.isDebugEnabled()) {
            log.debug("Inquire topic status and got responses size: {}", Integer.valueOf(responses.length));
        }
        for (PCFMessage response : responses) {
            StatisticalData object = Metadata.newStatisticalDataFor(monitor);
            PCFMessage[] subResponses = sendRequest(monitor.getConnection(), this.inquireSubStatusRequest, this.timeout, autoConnect);
            if (log.isDebugEnabled()) {
                log.debug("Inquire topic sub status and got responses size: {}", Integer.valueOf(subResponses.length));
            }
            final int publishedMessagesCount = subResponses[0].getIntParameterValue(1290);
            if (log.isDebugEnabled()) {
                log.debug("Got topic publishedMessagesCount: {}", Integer.valueOf(publishedMessagesCount));
            }
            TopicData topicData = (TopicData) object.adapt(TopicData.class);
            topicData.setPublishedMessagesCount(publishedMessagesCount);
            this.setHealthEvaluationContext(context, new HashMap<String, Object>(1) {
                {
                    this.put("publishedMessagesCount", publishedMessagesCount);
                }
            });
            object = handleResponse((StatisticalData) object, monitor, response, context, true);
            if (!isMeasureOnly(context)) {
                object = getTopicData((StatisticalData) object, monitor, context);
            }
            dataList.add(object);
        }
        return dataList;
    }

    private TopicData getTopicData(StatisticalData data, Monitor monitor, Map<String, Object> context) throws Exception {
        PCFMessage[] topicResponses = sendRequest(monitor.getConnection(), this.inquireTopicRequest, this.timeout,
                isAutoConnect(context));
        if (log.isDebugEnabled()) {
            log.debug("Inquire topic and got responses size: {}", Integer.valueOf(topicResponses.length));
        }
        data = this.handleResponse(data, monitor, topicResponses[0], (Map) (context == null ? new HashMap<String, Object>(1) {
            {
                this.put("context.measureonly", false);
            }
        } : context), true);
        if (log.isDebugEnabled()) {
            log.debug("Got topic data: {}", data);
        }
        return (TopicData) data.adapt(TopicData.class);
    }
}
