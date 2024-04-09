package com.jcca.web.ibmMQ.health.impl;


import com.jcca.web.ibmMQ.domain.HealthMessage;
import com.jcca.web.ibmMQ.health.HealthEvaluationException;
import com.jcca.web.ibmMQ.health.HealthResult;
import com.jcca.web.ibmMQ.health.IHealthEvaluator;
import com.jcca.web.ibmMQ.vo.HealthState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.concurrent.ThreadSafe;
import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


@Component("healthEvaluator")
@ThreadSafe
public class HealthEvaluator implements IHealthEvaluator {
    private static final Logger log = LoggerFactory.getLogger(HealthEvaluator.class);

    private final HealthRuleParser healthRuleParser = new HealthRuleParser();

    private final ScriptEngine engine = (new ScriptEngineManager()).getEngineByName("javascript");


    public void validate(String rules) {
        parse(rules);
    }

    private Set<HealthRule> parse(String rules) {
        return this.healthRuleParser.parserRules(rules);
    }


    public HealthResult evaluate(String rules, Map<String, Object> context) {

        Set<HealthRule> healthRules = parse(rules);

        HealthResult healthResult = null, prevResult = null;

        Iterator<HealthRule> iter = healthRules.iterator();

        while (iter.hasNext()) {

            HealthRule rule = iter.next();

            healthResult = evaluate(rule, context);

            if (log.isDebugEnabled()) {

                log.debug("The evaluation result for rule '{}' is '{}'", rule, healthResult);

            }

            if (prevResult != null) {

                healthResult = healthResult.combine(prevResult);

            }

            if (log.isDebugEnabled()) {

                log.debug("The combined result with rule '{}' is '{}'", rule, healthResult);

            }

            if (healthResult.getHealthState() == HealthState.OK) {

                break;

            }

            prevResult = healthResult;

        }

        if (log.isDebugEnabled()) {

            log.debug("The final result is '{}'", healthResult);

        }

        return healthResult;

    }


    private HealthResult evaluate(HealthRule healthRule, Map<String, Object> context) {

        HealthResult healthResult = new HealthResult();

        try {

            Bindings bindings = this.engine.createBindings();

            for (Map.Entry<String, Object> entry : context.entrySet()) {

                Object value = entry.getValue();

                String key = entry.getKey();

                if (value instanceof Enum) {

                    bindings.put(key, ((Enum) value).name());

                    continue;

                }

                bindings.put(key, value.toString());

            }

            boolean ruleMatches = !(healthRule.getRuleType() == HealthRule.RuleType.MATCH_ANY);

            for (HealthRuleEntry entry : healthRule.getEntries()) {

                Object result = this.engine.eval(entry.toEvaluableString(), bindings);

                if (log.isDebugEnabled()) {

                    log.debug("HealthRuleEntry '{}' eval result is '{}'", entry, result);

                }

                if (result instanceof Boolean) {

                    boolean entryMatches = ((Boolean) result).booleanValue();

                    switch (healthRule.getRuleType()) {
                        case MATCH_ANY:
                            ruleMatches = (ruleMatches || entryMatches);

                            break;

                        case MATCH_ALL:

                            ruleMatches = (ruleMatches && entryMatches);
                            break;
                        default:
                            ruleMatches = (ruleMatches && entryMatches);
                            break;

                    }
                    if (entryMatches) {

                        Object actualValue = bindings.get(entry.getLeft());

                        HealthMessage message = new HealthMessage();

                        message.setRule(entry.toExpression());

                        message.setResult(String.format("%s=%s", new Object[]{entry.getLeft(), actualValue}));

                        message.setLevel(healthRule.getTargetHealthState());

                        healthResult.addMessage(message);

                        if (log.isDebugEnabled()) {

                            log.debug(message.toString());

                        }

                    }

                }

            }

            if (log.isDebugEnabled()) {

                log.debug("Evaluation result health is '{}'", Boolean.valueOf(!ruleMatches));

            }

            if (ruleMatches) {

                healthResult.setHealthState(healthRule.getTargetHealthState());

            } else {

                healthResult.getMessages().clear();

            }

            bindings = null;

        } catch (ScriptException e) {

            throw new HealthEvaluationException(String.format("Failed to evaluate rule '%s', cause: '%s'!", new Object[]{healthRule, e
                    .getMessage()}), e);

        }

        return healthResult;

    }
}

