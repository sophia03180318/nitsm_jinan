package com.jcca.web.ibmMQ.command.impl;


import com.ibm.mq.pcf.PCFMessage;
import com.jcca.web.ibmMQ.common.Time;
import com.jcca.web.ibmMQ.domain.Monitor;
import com.jcca.web.ibmMQ.domain.StatisticalData;
import com.jcca.web.ibmMQ.health.HealthEvaluationException;
import com.jcca.web.ibmMQ.health.HealthResult;
import com.jcca.web.ibmMQ.health.HealthRuleException;
import com.jcca.web.ibmMQ.health.IHealthEvaluator;
import com.jcca.web.ibmMQ.util.Assert;
import com.jcca.web.ibmMQ.util.Metadata;
import com.jcca.web.ibmMQ.util.Strings;
import com.jcca.web.ibmMQ.vo.HealthState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/*     */

public abstract class InquireDataCommand extends InquireCommand {
    private static final Logger log = LoggerFactory.getLogger(InquireDataCommand.class);

    @Resource(name = "healthEvaluator")
    private IHealthEvaluator healthEvaluator;

    protected int getTimeout(Monitor monitor) {
        Time time = Time.parseSMHD(monitor.getPollingInterval());
        int timeout = (int) time.convertTo(TimeUnit.SECONDS);
        if (log.isDebugEnabled()) {
            log.debug("Request timeout: {} seconds.", Integer.valueOf(timeout));
        }
        return timeout;
    }

    protected StatisticalData handleResponse(Monitor monitor, PCFMessage response, Map<String, Object> context) throws Exception {
        return handleResponse(null, monitor, response, context, true);
    }

    protected StatisticalData handleResponse(Monitor monitor, PCFMessage response, Map<String, Object> context, boolean healthEvaluationReady) throws Exception {
        return handleResponse(null, monitor, response, context, healthEvaluationReady);
    }

    protected StatisticalData handleResponse(StatisticalData data, Monitor monitor, PCFMessage response, Map<String, Object> context, boolean healthEvaluationReady) throws Exception {
        boolean measureOnly = isMeasureOnly(context);
        Metadata metadata = Metadata.valueOf(monitor.getCategory());
        Assert.notNull(metadata, "metadata cannot be null!");
        final Map<String, Object> evaluationContext = new HashMap<String, Object>();
        data = metadata.invoke(data, monitor, response, measureOnly, new Metadata.Listener() {
            public void onPropertySet(String field, Object value) {
                evaluationContext.put(field, value);
                if (log.isDebugEnabled()) {
                    log.debug("Metadata.Listener got field '{}', value:'{}'", field, value);
                }
            }
        });
        if (!healthEvaluationReady) {
            if (log.isDebugEnabled()) {
                log.debug("Health evaluation is not ready and skipped temparily!");
            }
            return data;
        }
        if (data.getHealthState() != null && !data.getHealthState().equals(HealthState.UNKNOWN.getValue())) {
            if (log.isDebugEnabled()) {
                log.debug("Health state of monitor '{}' is ready evaluated as '{}', just skip it.", monitor.getName(), data
                        .getHealthState());
            }
            return data;
        }
        try {
            evaluationContext.putAll(getHealthEvaluationContext(context));
            if (log.isDebugEnabled()) {
                log.debug("Evaluation context for monitor '{}' is '{}'", monitor.getName(), evaluationContext);
            }
            HealthResult result = evaluateHealth(monitor, evaluationContext);
            data.setHealthState(result.getHealthState());
            if (result.getHealthState() != HealthState.OK) {
                data.addMessages(result.getMessages());
            }
            clearHealthEvaluationContext(context);
        } catch (HealthRuleException e) {
            log.error("Health rule error, failed to check health state!", (Throwable) e);
            data.setHealthState(HealthState.UNKNOWN);
        } catch (HealthEvaluationException e) {
            log.error("Health evaluation failed, set health state to UNKNOWN !", (Throwable) e);
            data.setHealthState(HealthState.UNKNOWN);
        }
        return data;
    }

    protected HealthResult evaluateHealth(Monitor monitor, Map<String, Object> evaluationContext) {
        String rule = getHealthRule(monitor);
        //如果没有规则，默认状态为正常状态
        if (rule == null) {
            if (log.isDebugEnabled()) {
                log.debug("No health rule available for monitor '{}', set health state to UNKNOWN!", monitor.getName());
            }
            return HealthResult.OK;
        }
        return this.healthEvaluator.evaluate(rule, evaluationContext);
    }

    private String getHealthRule(Monitor monitor) {
        String rule = monitor.getHealthRule();
        if (Strings.isNullOrEmpty(rule)) {
            switch (monitor.getCategory()) {
                case Queue:
                    rule = this.configuration.getQueueHealthRule();
                    break;
                case Topic:
                    rule = this.configuration.getTopicHealthRule();
                    break;
                case Channel:
                    rule = this.configuration.getChannelHealthRule();
                    break;
                case Listener:
                    rule = this.configuration.getListenerHealthRule();
                    break;
                case QueueManager:
                    rule = this.configuration.getQueueManagerHealthRule();
                    break;
            }
        }
        return rule;
    }
}
