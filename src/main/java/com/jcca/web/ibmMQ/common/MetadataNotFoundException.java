package com.jcca.web.ibmMQ.common;


public class MetadataNotFoundException extends MetadataException {
    private static final long serialVersionUID = 1L;


    public MetadataNotFoundException(int errorCode, String messageId, Object... bindings) {
        super(errorCode, messageId, bindings);
    }

}


