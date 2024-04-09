package com.jcca.web.ibmMQ.common;

public class LocalizedIllegalArgumentException extends LocalizedMonitoringException {
    private static final long serialVersionUID = 1L;

    public LocalizedIllegalArgumentException(String messageId, Object[] bindings, Throwable cause) {
        super(messageId, bindings, cause);
    }

    public LocalizedIllegalArgumentException(String messageId, Throwable cause) {
        super(messageId, cause);
    }

    public LocalizedIllegalArgumentException(String messageId, Object... bindings) {
        super(messageId, bindings);
    }
}
