package com.jcca.web.ibmMQ.schedule;

public class MonitorNotScheduledException extends MonitorSchedulerException {
    private static final long serialVersionUID = 1L;


    public MonitorNotScheduledException(String messageId, Object... bindings) {
        super(messageId, bindings);
    }

}

