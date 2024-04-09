package com.jcca.web.ibmMQ.common;


public class NotConnectedException extends LocalizedMonitoringException {
    private static final long serialVersionUID = 1L;


    public NotConnectedException(int errorCode, String messageId, Object... bindings) {
        super(errorCode, messageId, bindings);
    }
}
