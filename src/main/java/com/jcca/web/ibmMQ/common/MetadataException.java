package com.jcca.web.ibmMQ.common;


public class MetadataException extends LocalizedMonitoringException {
    private static final long serialVersionUID = 1L;

    public MetadataException(int errorCode, String messageId, Object[] bindings, Throwable cause) {
        super(errorCode, messageId, bindings, cause);
    }

    public MetadataException(String messageId, Object[] bindings, Throwable cause) {
        super(messageId, bindings, cause);
    }

    public MetadataException(int errorCode, String messageId, Object[] bindings) {
        super(errorCode, messageId, bindings);
    }

    public MetadataException(int errorCode, String messageId, Throwable cause) {
        super(errorCode, messageId, cause);
    }

    public MetadataException(int errorCode, String messageId) {
        super(errorCode, messageId);
    }

    public MetadataException(String messageId, Throwable cause) {
        super(messageId, cause);
    }
}

