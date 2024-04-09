package com.jcca.web.ibmMQ.common;

public class StatisticalDataNotFoundException
        extends StatisticalDataException {
    private static final long serialVersionUID = 1L;

    public StatisticalDataNotFoundException(int errorCode, String messageId, Object... bindings) {
        super(errorCode, messageId, bindings);
    }

}