package com.jcca.web.ibmMQ.common;


public class MetadataAccessNotAllowedException extends MetadataException {
    private static final long serialVersionUID = 1L;

    public MetadataAccessNotAllowedException(int errorCode, String messageId, Object... bindings) {
        super(errorCode, messageId, bindings);
    }
}