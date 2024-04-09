package com.jcca.web.ibmMQ.common;


public class IllegalMetadataStateException extends MetadataException {
    private static final long serialVersionUID = 1L;


    public IllegalMetadataStateException(int errorCode, String messageId, Object[] bindings, Throwable cause) {
        super(errorCode, messageId, bindings, cause);
    }


    public IllegalMetadataStateException(int errorCode, String messageId, Object... bindings) {
        super(errorCode, messageId, bindings);
    }

}


