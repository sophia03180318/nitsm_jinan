package com.jcca.web.ibmMQ.config;


public class ConfigurationNotFoundException extends ConfigurationException {
    private static final long serialVersionUID = 1L;


    public ConfigurationNotFoundException(int errorCode, String messageId, Object... bindings) {
        super(errorCode, messageId, bindings);
    }
}


