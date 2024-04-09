package com.jcca.web.ibmMQ.command.impl;


import com.jcca.web.ibmMQ.command.CommandException;
import com.jcca.web.ibmMQ.command.CommandNotFoundException;
import com.jcca.web.ibmMQ.command.ICommand;
import com.jcca.web.ibmMQ.command.ICommandRegistry;
import com.jcca.web.ibmMQ.domain.Monitor;
import com.jcca.web.ibmMQ.support.BeanLocator;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;


@Component("commandRegistry")
public class CommandRegistry implements ICommandRegistry {
    private final Map<Monitor.Category, String> commandNameMapping = new HashMap<Monitor.Category, String>();

    @Resource(name = "beanLocator")
    private BeanLocator beanLocator;


    @PostConstruct
    private void init() {
        this.commandNameMapping.put(Monitor.Category.Channel, "INQUIRE_CHANNEL");
        this.commandNameMapping.put(Monitor.Category.Queue, "INQUIRE_Q");
        this.commandNameMapping.put(Monitor.Category.Topic, "INQUIRE_TOPIC");
        this.commandNameMapping.put(Monitor.Category.QueueManager, "INQUIRE_QMGR");
        this.commandNameMapping.put(Monitor.Category.Listener, "INQUIRE_LISTENER");
    }

    public ICommand getCommand(Monitor monitor) {
        String commandName = this.commandNameMapping.get(monitor.getCategory());
        if (commandName == null) {
            throw new CommandNotFoundException(String.format("No Command available for monitor category %s", new Object[]{monitor
                    .getCategory()}));
        }
        return getCommand0(commandName);
    }


    public ICommand getCommand(String commandName) {
        return getCommand0(commandName);
    }


    private ICommand getCommand0(String commandName) {
        try {
            return (ICommand) this.beanLocator.findBean(commandName, ICommand.class);
        } catch (NoSuchBeanDefinitionException e) {
            throw new CommandNotFoundException(String.format("No Command available with name '%s'", new Object[]{commandName}));
        } catch (BeansException ex) {
            throw new CommandException(String.format("Failed to get command with name '%s'", new Object[]{commandName}), (Throwable) ex);
        }
    }
}
