package com.jcca.web.ibmMQ.command;

public class CommandNotFoundException extends CommandException {
    private static final long serialVersionUID = 1L;


    public CommandNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }


    public CommandNotFoundException(String message) {
        super(message);
    }


    public CommandNotFoundException(Throwable cause) {
        super(cause);
    }

}