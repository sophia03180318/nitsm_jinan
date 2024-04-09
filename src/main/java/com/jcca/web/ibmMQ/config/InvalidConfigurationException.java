package com.jcca.web.ibmMQ.config;


public class InvalidConfigurationException extends ConfigurationException {
    private static final long serialVersionUID = 1L;

    public InvalidConfigurationException(int errorCode, String messageId, Object... bindings) {
        super(errorCode, messageId, bindings);
    }
}

