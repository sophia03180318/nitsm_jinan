package com.jcca.web.ibmMQ.health;


import com.jcca.web.ibmMQ.common.MonitoringException;

public class HealthEvaluationException extends MonitoringException {
    private static final long serialVersionUID = 1L;


    public HealthEvaluationException(String message, Throwable cause) {
        super(message, cause);
    }


    public HealthEvaluationException(String message) {
        super(message);
    }

    public HealthEvaluationException(Throwable cause) {
        super(cause);
    }
    /*    */
}

