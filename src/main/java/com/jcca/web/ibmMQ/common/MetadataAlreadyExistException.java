package com.jcca.web.ibmMQ.common;


public class MetadataAlreadyExistException extends MetadataException {
    private static final long serialVersionUID = 1L;

    public MetadataAlreadyExistException(int errorCode, String messageId, Object... bindings) {
        super(errorCode, messageId, bindings);
    }
}
