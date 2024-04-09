package com.jcca.web.ibmMQ.command.impl;


import com.google.common.collect.Maps;
import com.jcca.web.ibmMQ.command.ICommand;
import com.jcca.web.ibmMQ.command.ICommandProcessor;
import com.jcca.web.ibmMQ.command.ICommandRegistry;
import com.jcca.web.ibmMQ.domain.Monitor;
import com.jcca.web.ibmMQ.util.Assert;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;

@Component("commandProcessor")
public class CommandProcessor implements ICommandProcessor {

    @Resource
    private ICommandRegistry commandRegistry;

    public Object process(String commandName, Map<String, Object> context) throws Exception {
        Assert.notNull(commandName, "Command name cannot be null!");
        return this.commandRegistry.getCommand(commandName).execute(newContext(context));
    }

    public Object process(ICommand command, Map<String, Object> context) throws Exception {
        Assert.notNull(command, "Command cannot be null!");
        return command.execute(newContext(context));
    }

    public Object process(Monitor monitor, Map<String, Object> context) throws Exception {
        Assert.notNull(monitor, "Monitor cannot be null!");
        ICommand command = this.commandRegistry.getCommand(monitor);
        return command.execute(newContext(context));
    }

    private Map<String, Object> newContext(Map<String, Object> inputContext) {
        Map<String, Object> context = Maps.newHashMap();
        if (inputContext != null) {
            context.putAll(inputContext);
        }
        return context;
    }
}