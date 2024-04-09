package com.jcca.web.ibmMQ.schedule;


import com.jcca.web.ibmMQ.common.LocalizedMonitoringException;

public class MonitorSchedulerException extends LocalizedMonitoringException {
    private static final long serialVersionUID = 1L;

    public MonitorSchedulerException(Throwable cause) {
        super(cause);
    }

    public MonitorSchedulerException(String messageId, Object... bindings) {
        super(messageId, bindings);
    }


    public MonitorSchedulerException(int errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}


