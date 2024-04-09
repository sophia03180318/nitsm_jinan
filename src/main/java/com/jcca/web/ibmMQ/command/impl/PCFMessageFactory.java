package com.jcca.web.ibmMQ.command.impl;


import com.ibm.mq.pcf.PCFMessage;
import com.jcca.web.ibmMQ.domain.ChannelData;
import com.jcca.web.ibmMQ.domain.Monitor;
import com.jcca.web.ibmMQ.domain.QueueData;
import com.jcca.web.ibmMQ.domain.TopicData;
import com.jcca.web.ibmMQ.util.Strings;

public final class PCFMessageFactory {

    public static PCFMessage createPingQMgr() {
        return new PCFMessage(40);
    }


    public static PCFMessage createInquireObjectName(String objectName, Monitor.Category category) {
        PCFMessage request = null;
        String name = objectName.trim();
        switch (category) {
            case Queue:
                request = new PCFMessage(13);
                request.addParameter(2016, name);
                request.addParameter(1002, new int[]{2016, 20});
                break;
            case Topic:
                request = new PCFMessage(174);
                request.addParameter(2092, name);
                request.addParameter(1269, new int[]{2092, 208});
                break;
            case Channel:
                request = new PCFMessage(25);
                request.addParameter(3501, name);
                request.addParameter(1015, new int[]{3501, 1511});
                break;
            case Listener:
                request = new PCFMessage(97);
                request.addParameter(3554, name);
                request.addParameter(1222, new int[]{3554});
                break;
            case QueueManager:
                throw new IllegalArgumentException("Queue manager name check is not applicable!");
        }
        return request;
    }

    public static PCFMessage createInquireListenerStatus(String listenerName) {
        PCFMessage request = new PCFMessage(98);
        request.addParameter(3554, listenerName.trim());
        return request;
    }

    public static PCFMessage createInquireListener(String listenerName) {
        PCFMessage request = new PCFMessage(97);
        request.addParameter(3554, listenerName.trim());
        return request;
    }

    public static PCFMessage createPingMQTTService() {
        PCFMessage request = new PCFMessage(25);
        request.addParameter(3501, "*");
        request.addParameter(1511, 10);
        request.addParameter(1015, new int[]{3501});
        return request;
    }

    public static PCFMessage createInquireMQTTChannelStatus(String channelName) {
        PCFMessage request = new PCFMessage(42);
        request.addParameter(3501, Strings.isNullOrEmpty(channelName) ? "*" : channelName.trim());
        request.addParameter(1511, 10);
        request.addParameter(3564, "*");
        return request;
    }

    public static PCFMessage createInquireChannelStatus(String channelName) {
        PCFMessage request = new PCFMessage(42);
        request.addParameter(3501, channelName.trim());
        return request;
    }

    public static PCFMessage createInquireMQTTChannel(String channelName) {
        PCFMessage request = new PCFMessage(25);
        request.addParameter(3501, channelName.trim());
        request.addParameter(1511, 10);
        return request;
    }

    public static PCFMessage createInquireChannel(String channelName) {
        PCFMessage request = new PCFMessage(25);
        request.addParameter(3501, channelName.trim());
        return request;
    }

    public static PCFMessage createInquireTopic(String topicName) {
        PCFMessage request = new PCFMessage(174);
        request.addParameter(2092, topicName.trim());
        return request;
    }

    public static PCFMessage createInquireTopicStatus(String topicString, int statusType) {
        PCFMessage request = new PCFMessage(183);
        request.addParameter(2094, topicString.trim());
        if (statusType != -1) {
            request.addParameter(1302, statusType);
        }
        return request;
    }

    public static PCFMessage createInquireSubscriptionStatus(String subscriptionName) {
        PCFMessage request = new PCFMessage(182);
        request.addParameter(3152, subscriptionName.trim());
        return request;
    }

    public static PCFMessage createInquireQ(String queueName) {
        PCFMessage request = new PCFMessage(13);
        request.addParameter(2016, queueName.trim());
        return request;
    }

    public static PCFMessage createInquireQChannels(String queueName) {
        PCFMessage request = new PCFMessage(41);
        request.addParameter(2016, queueName.trim());
        request.addParameter(1103, 1104);
        request.addParameter(1026, new int[]{3501});
        return request;
    }

    public static PCFMessage createInquireQMgr() {
        PCFMessage request = new PCFMessage(2);
        request.addParameter(1001, new int[]{32, 31, 2120});
        return request;
    }

    public static PCFMessage createInquireQMgrV7() {
        PCFMessage request = new PCFMessage(2);
        request.addParameter(1001, new int[]{32, 31});
        return request;
    }

    public static PCFMessage createInquireQMgrStatus() {
        PCFMessage request = new PCFMessage(161);
        return request;
    }

    public static PCFMessage createInquireListenerNames(String listenerName) {
        PCFMessage request = new PCFMessage(97);
        request.addParameter(3554, Strings.isNullOrEmpty(listenerName) ? "*" : listenerName.trim());
        request.addParameter(1222, new int[]{3554});
        return request;
    }

    public static PCFMessage createInquireChannelNames(String channelName, ChannelData.ChannelType type) {
        PCFMessage request = new PCFMessage(20);
        request.addParameter(3501, Strings.isNullOrEmpty(channelName) ? "*" : channelName.trim());
        request.addParameter(1511, (type == null) ? 5 : type.getValue());
        return request;
    }

    public static PCFMessage createInquireQNames(String queueName, QueueData.QueueType type) {
        PCFMessage request = new PCFMessage(18);
        request.addParameter(2016, Strings.isNullOrEmpty(queueName) ? "*" : queueName.trim());
        request.addParameter(20, (type == null) ? 1001 : type.getValue());
        return request;
    }

    public static PCFMessage createInquireTopicNames(String topicName, TopicData.TopicType type) {
        PCFMessage request = new PCFMessage(174);
        request.addParameter(2092, Strings.isNullOrEmpty(topicName) ? "*" : topicName.trim());
        request.addParameter(208, (type == null) ? 2 : type.getValue());
        request.addParameter(1269, new int[]{2092, 208, 2094});
        return request;
    }

    public static PCFMessage createSubscription(String subscriptionName, String topicString, String destinationQueue) {
        PCFMessage request = new PCFMessage(177);
        request.addParameter(3152, subscriptionName);
        request.addParameter(2094, topicString);
        request.addParameter(3154, destinationQueue);
        return request;
    }

    public static PCFMessage deleteSubscription(String subscriptionName) {
        PCFMessage request = new PCFMessage(179);
        request.addParameter(3152, subscriptionName);
        return request;
    }

    public static PCFMessage createQueue(String queueName, QueueData.QueueType queueType) {
        return createQueue(queueName, queueType, 0);
    }

    public static PCFMessage createQueue(String queueName, QueueData.QueueType queueType, int maxQDepth) {
        PCFMessage request = new PCFMessage(11);
        request.addParameter(2016, queueName);
        request.addParameter(20, queueType.getValue());
        if (maxQDepth <= 0) {
            maxQDepth = 640000;
        }
        request.addParameter(15, maxQDepth);
        return request;
    }

    public static PCFMessage deleteQueue(String queueName) {
        PCFMessage request = new PCFMessage(12);
        request.addParameter(2016, queueName);
        request.addParameter(1007, 1);
        return request;
    }

    public static PCFMessage clearQueue(String queueName) {
        PCFMessage request = new PCFMessage(9);
        request.addParameter(2016, queueName);
        return request;
    }
}