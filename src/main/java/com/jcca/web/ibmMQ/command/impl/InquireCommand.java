package com.jcca.web.ibmMQ.command.impl;


import com.google.common.collect.Maps;
import com.ibm.mq.constants.MQConstants;
import com.ibm.mq.pcf.PCFMessage;
import com.jcca.web.ibmMQ.command.CommandException;
import com.jcca.web.ibmMQ.command.ICommand;
import com.jcca.web.ibmMQ.config.Configuration;
import com.jcca.web.ibmMQ.domain.Connection;
import com.jcca.web.ibmMQ.domain.Monitor;
import com.jcca.web.ibmMQ.service.IMetadataService;
import com.jcca.web.ibmMQ.service.PCFMessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;


@Component
public abstract class InquireCommand implements ICommand {
    private static final Logger log = LoggerFactory.getLogger(InquireCommand.class);

    private static final String SYSTEM_OBJECT_PREFIX = "SYSTEM.";

    private static final String DYN_OBJECT_PREFIX = "AMQ.";

    @Resource(name = "messageService")
    protected PCFMessageService messageService;

    @Resource(name = "metadataService")
    protected IMetadataService metadataService;

    @Resource(name = "configuration")
    protected Configuration configuration;

    protected PCFMessage[] sendRequestWithoutCheck(Connection connection, PCFMessage request, int timeout) throws Exception {
        return this.messageService.sendRequest(connection, request, timeout, false);
    }


    protected PCFMessage[] sendRequest(Connection connection, PCFMessage request) throws Exception {
        return sendRequest(connection, request, 0, false);
    }

    protected PCFMessage[] sendRequest(Connection connection, PCFMessage request, int timeout, boolean autoConnect) throws Exception {
        PCFMessage[] responses = this.messageService.sendRequest(connection, request, timeout, autoConnect);
        if (log.isDebugEnabled()) {
            log.debug("Checking responses...");
        }
        if (responses.length <= 2 && responses[0].getCompCode() != 0)
            throw new CommandException(String.format("PCF reponse error: Complete Code: %d, Reason Code: %d (%s)", new Object[]{
                    Integer.valueOf(responses[0].getCompCode()), Integer.valueOf(responses[0].getReason()), MQConstants.lookupReasonCode(responses[0].getReason())
            }));
        if (log.isDebugEnabled()) {
            log.debug("Checked responses ok");
        }
        return responses;
    }

    protected Monitor getMonitor(Map<String, Object> context) {
        return (Monitor) context.get("context.monitor");
    }

    protected Connection getConnection(Map<String, Object> context) {
        return (Connection) context.get("context.connection");
    }

    protected Map<String, Object> getHealthEvaluationContext(Map<String, Object> context) {
        Map<String, Object> evaluationContext = (Map<String, Object>) context.get("context.health.evaluation");
        if (evaluationContext != null) {
            return evaluationContext;
        }
        return Maps.newHashMap();
    }

    protected static boolean isInquireObjectNamesFailed(int rc) {
        return (rc == 4008 || rc == 2085);
    }

    protected void clearHealthEvaluationContext(Map<String, Object> context) {
        context.remove("context.health.evaluation");
    }

    protected void setHealthEvaluationContext(Map<String, Object> context, Map<String, Object> evaluationContext) {
        context.put("context.health.evaluation", evaluationContext);
    }

    protected boolean isAutoConnect(Map<String, Object> context) {
        if (context == null) {
            return false;
        }
        Object autoConnect = context.get("context.autoConnect");
        return (autoConnect == null) ? false : ((Boolean) context.get("context.autoConnect")).booleanValue();
    }

    protected String getObjectName(Map<String, Object> context) {
        return (String) context.get("context.object.name");
    }

    protected Monitor.Category getObjectCategory(Map<String, Object> context) {
        return (Monitor.Category) context.get("context.object.category");
    }

    protected <T> T getObjectType(Map<String, Object> context, Class<T> type) {
        return (T) context.get("context.object.type");
    }

    protected String getSubscriptionName(Map<String, Object> context) {
        return (String) context.get("context.subscription.name");
    }

    protected String getSubscriptionTopicString(Map<String, Object> context) {
        return (String) context.get("context.subscription.topicstring");
    }

    protected String getSubscriptionDestination(Map<String, Object> context) {
        return (String) context.get("context.subscription.destination");
    }

    protected boolean isMeasureOnly(Map<String, Object> context) {
        Boolean measureonly = (Boolean) context.get("context.measureonly");
        return (measureonly == null) ? true : measureonly.booleanValue();
    }

    protected boolean isSystemObjectName(String name) {
        return (!name.startsWith("SYSTEM.") && !name.startsWith("AMQ."));
    }
}
