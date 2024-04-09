package com.jcca.web.ibmMQ.command;


import com.jcca.web.ibmMQ.domain.Monitor;

public interface ICommandRegistry {
    ICommand getCommand(String paramString);

    ICommand getCommand(Monitor paramMonitor);
}


