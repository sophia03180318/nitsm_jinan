package com.jcca.web.ibmMQ.health;


import com.jcca.web.ibmMQ.common.LocalizedMonitoringException;

public class HealthRuleException extends LocalizedMonitoringException {
    private static final long serialVersionUID = 1L;

    public HealthRuleException(String messageId, Object... bindings) {
        super(messageId, bindings);
    }
}

