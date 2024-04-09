package com.jcca.web.ibmMQ.common;


public class MonitoringException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    private int errorCode;

    public MonitoringException(int errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }


    public MonitoringException(int errorCode, String message) {
        super(message);
        this.errorCode = errorCode;

    }


    public MonitoringException(String message, Throwable cause) {
        super(message, cause);
    }

    public MonitoringException(String message) {
        super(message);
    }

    public MonitoringException(Throwable cause) {
        super(cause);
    }

    public int getErrorCode() {
        return this.errorCode;
    }
    /*    */
}


