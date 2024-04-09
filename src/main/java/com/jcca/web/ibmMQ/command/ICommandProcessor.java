package com.jcca.web.ibmMQ.command;

import com.jcca.web.ibmMQ.domain.Monitor;

import java.util.Map;

public interface ICommandProcessor {
    Object process(String paramString, Map<String, Object> paramMap) throws Exception;

    Object process(ICommand paramICommand, Map<String, Object> paramMap) throws Exception;

    Object process(Monitor paramMonitor, Map<String, Object> paramMap) throws Exception;
}
