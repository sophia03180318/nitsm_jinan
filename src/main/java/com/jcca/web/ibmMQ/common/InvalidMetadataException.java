package com.jcca.web.ibmMQ.common;


public class InvalidMetadataException extends MetadataException {
    private static final long serialVersionUID = 1L;

    public InvalidMetadataException(int errorCode, String messageId, Object[] bindings, Throwable cause) {
        super(errorCode, messageId, bindings, cause);
    }

    public InvalidMetadataException(int errorCode, String messageId, Object... bindings) {
        super(errorCode, messageId, bindings);
    }
}


