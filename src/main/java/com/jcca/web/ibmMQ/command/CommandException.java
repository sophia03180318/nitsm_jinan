package com.jcca.web.ibmMQ.command;


import com.jcca.web.ibmMQ.common.MonitoringException;

public class CommandException extends MonitoringException {
    private static final long serialVersionUID = 1L;


    public CommandException(String message, Throwable cause) {
        super(message, cause);
    }


    public CommandException(String message) {
        super(message);
    }


    public CommandException(Throwable cause) {
        super(cause);
    }
}

