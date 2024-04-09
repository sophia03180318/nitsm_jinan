package com.jcca.web.ibmMQ.config;

import com.jcca.web.ibmMQ.common.LocalizedMonitoringException;

public class ConfigurationException extends LocalizedMonitoringException {
    private static final long serialVersionUID = 1L;


    public ConfigurationException(int errorCode, String messageId, Object[] bindings, Throwable cause) {
        super(errorCode, messageId, bindings, cause);
    }


    public ConfigurationException(int errorCode, String messageId, Object... bindings) {
        super(errorCode, messageId, bindings);
    }
}

