package com.jcca.web.ibmMQ.common;


public class StatisticalDataNotAvailableException extends StatisticalDataException {
    private static final long serialVersionUID = 1L;


    public StatisticalDataNotAvailableException(int errorCode, String messageId, Object... bindings) {
        super(errorCode, messageId, bindings);
    }

}

