package com.jcca.web.ibmMQ.schedule;

public class MonitorScheduleNotAllowedException extends MonitorSchedulerException {
    private static final long serialVersionUID = 1L;


    public MonitorScheduleNotAllowedException(String messageId, Object... bindings) {
        super(messageId, bindings);
    }
}

