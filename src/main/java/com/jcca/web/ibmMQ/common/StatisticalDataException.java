package com.jcca.web.ibmMQ.common;


public class StatisticalDataException extends LocalizedMonitoringException {
    private static final long serialVersionUID = 1L;

    public StatisticalDataException(int errorCode, String messageId, Object[] bindings, Throwable cause) {
        super(errorCode, messageId, bindings, cause);
    }

    public StatisticalDataException(int errorCode, String messageId, Object... bindings) {
        super(errorCode, messageId, bindings);
    }
}
