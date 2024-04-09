package com.jcca.web.ibmMQ.command;

import java.util.Map;

public interface ICommand {
    public static final String CONTEXT_REQUEST = "context.request";

    public static final String CONTEXT_CONNECTION = "context.connection";

    public static final String CONTEXT_MONITOR = "context.monitor";

    public static final String CONTEXT_MEASUREONLY = "context.measureonly";

    public static final String CONTEXT_OBJECT_NAME = "context.object.name";

    public static final String CONTEXT_OBJECT_TYPE = "context.object.type";

    public static final String CONTEXT_OBJECT_CATEGORY = "context.object.category";

    public static final String CONTEXT_SUBSCRIPTION_NAME = "context.subscription.name";

    public static final String CONTEXT_SUBSCRIPTION_TOPICSTRING = "context.subscription.topicstring";

    public static final String CONTEXT_SUBSCRIPTION_DESTINATION = "context.subscription.destination";

    public static final String CONTEXT_HEALTH_EVALUATION = "context.health.evaluation";

    public static final String CONTEXT_AUTO_CONNECT = "context.autoConnect";

    public static final String INQUIRE_Q = "INQUIRE_Q";

    public static final String INQUIRE_TOPIC = "INQUIRE_TOPIC";

    public static final String INQUIRE_TOPIC_STRING = "INQUIRE_TOPIC_STRING";

    public static final String INQUIRE_CHANNEL = "INQUIRE_CHANNEL";

    public static final String INQUIRE_MQTT_CHANNEL = "INQUIRE_MQTT_CHANNEL";

    public static final String INQUIRE_LISTENER = "INQUIRE_LISTENER";

    public static final String INQUIRE_QMGR = "INQUIRE_QMGR";

    public static final String INQUIRE_QMGR_NAME = "INQUIRE_QMGR_NAME";

    public static final String INQUIRE_Q_NAMES = "INQUIRE_Q_NAMES";

    public static final String INQUIRE_TOPIC_NAMES = "INQUIRE_TOPIC_NAMES";

    public static final String INQUIRE_CHANNEL_NAMES = "INQUIRE_CHANNEL_NAMES";

    public static final String INQUIRE_LISTENER_NAMES = "INQUIRE_LISTENER_NAMES";

    public static final String INQUIRE_OBJECT_TYPE = "INQUIRE_OBJECT_TYPE";

    public static final String EXIST_OBJECT_NAME = "EXIST_OBJECT_NAME";

    public static final String PING_QMGR = "PING_QMGR";

    public static final String PING_MQTT = "PING_MQTT";

    public static final String CREATE_SUBSCRIPTION = "CREATE_SUBSCRIPTION";

    public static final String DELETE_SUBSCRIPTION = "DELETE_SUBSCRIPTION";

    public static final String CREATE_QUEUE = "CREATE_QUEUE";

    public static final String DELETE_QUEUE = "DELETE_QUEUE";

    public static final String CLEAR_QUEUE = "CLEAR_QUEUE";

    Object execute(Map<String, Object> paramMap) throws Exception;
}
