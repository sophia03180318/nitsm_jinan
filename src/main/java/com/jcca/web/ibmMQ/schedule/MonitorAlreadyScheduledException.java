package com.jcca.web.ibmMQ.schedule;


public class MonitorAlreadyScheduledException extends MonitorSchedulerException {
    private static final long serialVersionUID = 1L;


    public MonitorAlreadyScheduledException(String messageId, Object... bindings) {
        super(messageId, bindings);
    }

}

