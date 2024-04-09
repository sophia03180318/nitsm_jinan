package com.jcca.web.ibmMQ.common;


import com.jcca.web.ibmMQ.util.NLSResource;

import java.util.Locale;

public final class ErrorConstants {
    private static final String RESOURCE_BUNDLE_BASENAME = "com.ibm.mq.monitoring.nls.message";

    public static class Code {
        public static final int INVALID_DASHBOARD = 40001;
        public static final int INVALID_CONNECTION = 40002;
        public static final int INVALID_MONITOR = 40003;
        public static final int INVALID_MONITOR_CATEGORY = 40004;
        public static final int INVALID_MONITOR_OBJECTTYPE = 40005;
        public static final int INVALID_MONITOR_OBJECTNAME = 40006;
        public static final int UNKNOWN_MONITOR_OBJECTNAME = 40007;
        public static final int INVALID_CONFIGURATION = 40008;
        public static final int CONNECTION_UNREACHABLE = 40009;
        public static final int CONNECTION_UNKNOWN_CHANNEL_NAME = 40010;
        public static final int CONNECTION_CHANNEL_NOT_AVAILABLE = 40011;
        public static final int INVALID_MESSAGE_SENDING_MODE = 40012;
        public static final int DASHBOARD_LAYOUT_REQUIRED = 40013;
        public static final int INVALID_DASHBOARD_LAYOUT = 40014;
        public static final int MQ_INVALID_DESTINATION = 40015;
        public static final int INVALID_USER_CREDENTIALS = 40016;
        public static final int DASHBOARD_TYPE_UPDATE_NOT_ALLOWED = 40301;
        public static final int CONNECTION_UPDATE_NOT_ALLOWED = 40302;
        public static final int CONNECTION_NOT_AUTHORIZED = 40303;
        public static final int MONITOR_CATEGORY_UPDATE_NOT_ALLOWED = 40304;
        public static final int MONITOR_OBJECTTYPE_UPDATE_NOT_ALLOWED = 40305;
        public static final int MONITOR_MEASUREMENT_UPDATE_NOT_ALLOWED = 40306;
        public static final int DEFAULT_MONITOR_UPDATE_NOT_ALLOWED = 40307;
        public static final int DEFAULT_MONITOR_DELETION_NOT_ALLOWED = 40308;
        public static final int MONITOR_ACTIVATION_NOT_ALLOWED = 40309;
        public static final int MONITOR_DELETION_NOT_ALLOWED = 40310;
        public static final int MQTT_MONITOR_ACCESS_NOT_ALLOWED = 40311;
        public static final int QMGR_MONITOR_DEACTIVATION_NOT_ALLOWED = 40312;
        public static final int DASHBOARD_NOT_FOUND = 40401;
        public static final int CONNECTION_NOT_FOUND = 40402;
        public static final int MONITOR_NOT_FOUND = 40403;
        public static final int CONFIGURATION_NOT_FOUND = 40404;
        public static final int STATISTICS_NOT_FOUND = 40405;
        public static final int MONITOR_NOT_FOUND_IN_DASHBOARD = 40406;
        public static final int USER_NOT_FOUND = 40407;
        public static final int DASHBOARD_ALREADY_EXISTS = 40901;
        public static final int CONNECTION_NOT_CONNECTED = 40902;
        public static final int CONNECTION_ALREADY_EXISTS = 40903;
        public static final int MONITOR_ALREADY_EXISTS = 40904;
        public static final int MONITOR_ALREADY_ACTIVATED = 40905;
        public static final int MONITOR_ALREADY_DEACTIVATED = 40906;
        public static final int STATISTICS_NOT_AVAILABLE = 40907;
        public static final int MONITOR_ALREADY_EXISTS_IN_DASHBOARD = 40908;
        public static final int MQTT_SERVICE_NOT_AVAILABLE = 40909;
        public static final int ILLEGAL_MONITOR_CATEGORY = 40910;
        public static final int MONITOR_NO_SUBSCRIPTION = 40911;
        public static final int MQ_OBJECT_ALREADY_EXISTS = 40912;
        public static final int MQ_SUB_ALREADY_EXISTS = 40913;
        public static final int MQ_UNKNOWN_OBJECTNAME = 40914;
        public static final int MQ_UNKNOWN_SUBSCRIPTION = 40915;
        public static final int MQ_Q_NOT_EMPTY = 40916;
        public static final int INTERNAL_SERVER_ERROR = 50000;

    }


    public static class Message extends NLSResource {
        public static String MSG_JAXRS_RESOURCE_NOT_FOUND;
        public static String MSG_JAXRS_METHOD_NOT_ALLOWED;
        public static String MSG_JAXRS_INTERNAL_SERVER_ERROR;
        public static String MSG_INVALID_METADATA_NAME;
        public static String MSG_INVALID_DASHBOARD;
        public static String MSG_INVALID_DASHBOARD_NAME;
        public static String MSG_DASHBOARD_ALREADY_EXISTS;
        public static String MSG_DASHBOARD_NOT_FOUND;
        public static String MSG_DASHBOARD_TYPE_UPDATE_NOT_ALLOWED;
        public static String MSG_MONITOR_NOT_FOUND_IN_DASHBOARD;
        public static String MSG_DASHBOARD_LAYOUT_REQUIRED;
        public static String MSG_INVALID_DASHBOARD_LAYOUT_SDRRCV;
        public static String MSG_MONITOR_ALREADY_EXISTS_IN_DASHBOARD;
        public static String MSG_INVALID_CONNECTION;
        public static String MSG_INVALID_CONNECTION_NAME;
        public static String MSG_INVALID_CONNECTION_CHANNELNAME;
        public static String MSG_INVALID_CONNECTION_HOST;
        public static String MSG_INVALID_CONNECTION_USERID;
        public static String MSG_INVALID_CONNECTION_PORT;
        public static String MSG_CONNECTION_ALREADY_EXISTS;
        public static String MSG_CONNECTION_NOT_FOUND;
        public static String MSG_CONNECTION_UPDATE_NOT_ALLOWED;
        public static String MSG_CONNECTION_NOT_CONNECTED;
        public static String MSG_INVALID_MONITOR;
        public static String MSG_INVALID_MONITOR_NAME;
        public static String MSG_INVALID_MONITOR_OBJECTNAME_1;
        public static String MSG_INVALID_MONITOR_OBJECTNAME_2;
        public static String MSG_INVALID_MONITOR_OBJECTNAME_3;
        public static String MSG_UNKNOWN_MONITOR_OBJECTNAME;
        public static String MSG_INVALID_MONITOR_CATEGORY_1;
        public static String MSG_INVALID_MONITOR_CATEGORY_2;
        public static String MSG_INVALID_MONITOR_OBJECTTYPE_1;
        public static String MSG_INVALID_MONITOR_OBJECTTYPE_2;
        public static String MSG_INVALID_MONITOR_MEASUREMENT_1;
        public static String MSG_INVALID_MONITOR_MEASUREMENT_2;
        public static String MSG_INVALID_MONITOR_TIMEVALUE_1;
        public static String MSG_INVALID_MONITOR_TIMEVALUE_2;
        public static String MSG_INVALID_MONITOR_TIMEVALUE_3;
        public static String MSG_INVALID_MONITOR_TIMEVALUE_4;
        public static String MSG_INVALID_MONITOR_TIMEVALUE_5;
        public static String MSG_INVALID_MONITOR_HEALTH_RULE_1;
        public static String MSG_INVALID_MONITOR_HEALTH_RULE_2;
        public static String MSG_INVALID_MONITOR_HEALTH_RULE_3;
        public static String MSG_INVALID_MESSAGE_SENDING_MODE;
        public static String MSG_MONITOR_SCHEDULE_NOT_ALLOWED_1;
        public static String MSG_MONITOR_SCHEDULE_NOT_ALLOWED_2;
        public static String MSG_MONITOR_ALREADY_SCHEDULED;
        public static String MSG_MONITOR_NOT_SCHEDULED;
        public static String MSG_MONITOR_SCHEDULER_CANCELLATION_FAILURE;
        public static String MSG_MONITOR_DIAGNOSER_CANCELLATION_FAILURE;
        public static String MSG_MONITOR_NOT_FOUND;
        public static String MSG_MONITOR_ALREADY_EXISTS;
        public static String MSG_MONITOR_CATEGORY_UPDATE_NOT_ALLOWED;
        public static String MSG_MONITOR_OBJECTTYPE_UPDATE_NOT_ALLOWED;
        public static String MSG_MONITOR_MEASUREMENT_UPDATE_NOT_ALLOWED;
        public static String MSG_DEFAULT_MONITOR_DELETION_NOT_ALLOWED;
        public static String MSG_DEFAULT_MONITOR_UPDATE_NOT_ALLOWED;
        public static String MSG_MONITOR_DELETION_NOT_ALLOWED;
        public static String MSG_STATISTICS_NOT_AVAILABLE;
        public static String MSG_ILLEGAL_MONITOR_CATEGORY;
        public static String MSG_MONITOR_NO_SUBSCRIPTION;
        public static String MSG_MQTT_MONITOR_ACCESS_NOT_ALLOWED;
        public static String MSG_QMGR_MONITOR_DEACTIVATION_NOT_ALLOWED;
        public static String MSG_MQ_OBJECT_ALREADY_EXISTS;
        public static String MSG_MQ_UNKNOWN_OBJECTNAME;
        public static String MSG_MQ_SUB_ALREADY_EXISTS;
        public static String MSG_MQ_INVALID_DESTINATION;
        public static String MSG_MQ_UNKNOWN_SUBSCRIPTION;
        public static String MSG_MQ_Q_NOT_EMPTY;
        public static String MSG_INVALID_USER_CREDENTIALS_1;
        public static String MSG_INVALID_USER_CREDENTIALS_2;
        public static String MSG_USER_NOT_FOUND;
        public static String MSG_MQTT_SERVICE_NOT_AVAILABLE;
        public static String MSG_CONFIGURATION_NOT_FOUND;
        public static String MSG_INVALID_CONFIGURATION;
        public static String MSG_CONNECTION_NOT_AUTHORIZED;
        public static String MSG_CONNECTION_UNREACHABLE;
        public static String MSG_CONNECTION_UNKNOWN_CHANNEL_NAME;
        public static String MSG_CONNECTION_CHANNEL_NOT_AVAILABLE;
        public static String MSG_STATISTICS_NOT_FOUND;
        public static String MSG_INVALID_TIME_FORMAT_1;
        public static String MSG_INVALID_TIME_FORMAT_MHD;
        public static String MSG_INVALID_TIME_FORMAT_SMHD;
        public static String MSG_ERROR_CODE_NOT_FOUND;
        public static String MSG_INTERNAL_SERVER_ERROR;


        public static String binds(String messageId, Object... bindings) {
            return bind("message", Locale.getDefault(), messageId, bindings);
        }


        public static String binds(Locale locale, String messageId, Object... bindings) {
            return bind("message", locale, messageId, bindings);
        }

        static {
            initialize(Message.class);
        }
    }
}
