package com.jcca.web.ibmMQ.common;


import com.jcca.web.ibmMQ.schedule.MonitorSchedulerException;


public class MonitorActivationNotAllowedException extends MonitorSchedulerException {
    private static final long serialVersionUID = 1L;

    public MonitorActivationNotAllowedException(int errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}

