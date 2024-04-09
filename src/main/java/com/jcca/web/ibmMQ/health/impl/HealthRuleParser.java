package com.jcca.web.ibmMQ.health.impl;


import com.jcca.web.ibmMQ.common.ErrorConstants;
import com.jcca.web.ibmMQ.health.HealthRuleException;
import com.jcca.web.ibmMQ.util.Strings;
import com.jcca.web.ibmMQ.vo.HealthState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.concurrent.ThreadSafe;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ThreadSafe
final class HealthRuleParser {
    private static final Logger log = LoggerFactory.getLogger(HealthRuleParser.class);

    private static final String MATH_OPERATORS = "!=<>";
    private static final String LOGICAL_OPERATORS = "&|";
    private static final String RULE_SEPERATOR = ",";
    private static final String REGEX_BASIC = String.format("\\(([^\\(%1$s]+)([%1$s]{1,2})([^\\)]+)\\)", new Object[]{"!=<>"});
    private static final String REGEX_FULL = String.format("\\(([%1$s])(?:%2$s)+\\)", new Object[]{"&|", REGEX_BASIC});

    private static final String L1 = String.format("(%s|%s)\\([^\\)]*\\)", new Object[]{HealthState.WARN, HealthState.ERROR});
    private static final String L2 = String.format("(%s|%s)\\([\\||&](\\([^\\)]*\\))+\\)", new Object[]{HealthState.WARN, HealthState.ERROR});

    private static final String REGEX_HEALTH_RULES = String.format("(%1$s|%2$s)(%3$s(%1$s|%2$s)){0,1}", new Object[]{L1, L2, ","});
    private final Pattern basicPattern = Pattern.compile(REGEX_BASIC);
    private final Pattern fullPattern = Pattern.compile(REGEX_FULL);
    private final Pattern healthRulesPattern = Pattern.compile(REGEX_HEALTH_RULES, 2);

    public Set<HealthRule> parserRules(String rules) {
        if (Strings.isNullOrEmpty(rules)) {
            throw new HealthRuleException(ErrorConstants.Message.MSG_INVALID_MONITOR_HEALTH_RULE_1, new Object[0]);
        }
        if (log.isDebugEnabled()) {
            log.debug("Parsing rules: {}", rules);
        }
        String normalizedRules = rules.replaceAll("\\s+", "");
        Matcher m = null;
        if ((m = this.healthRulesPattern.matcher(normalizedRules)).matches()) {
            Set<HealthRule> healthRules = new TreeSet<HealthRule>();
            HealthRule rule = parserRule(m.group(1));
            healthRules.add(rule);
            int end = m.end(1);
            if (end < normalizedRules.length()) {
                rule = parserRule(normalizedRules.substring(end + 1));
                healthRules.add(rule);
            }
            return healthRules;
        }
        throw new HealthRuleException(ErrorConstants.Message.MSG_INVALID_MONITOR_HEALTH_RULE_2, new Object[]{normalizedRules});
    }

    private HealthRule parserRule(String rule) {
        if (log.isDebugEnabled()) {
            log.debug("Parsing rule: {}", rule);
        }
        HealthState thresholdHealthState = HealthState.WARN;
        if (rule.toUpperCase().startsWith(HealthState.ERROR.name())) {
            thresholdHealthState = HealthState.ERROR;
        }
        String str = rule.substring(thresholdHealthState.name().length());
        if (log.isDebugEnabled()) {
            log.debug("Matching rule: {}", str);
        }
        Matcher basic = this.basicPattern.matcher(str);
        HealthRule healthRule = new HealthRule(thresholdHealthState);
        boolean matches = false;
        if (basic.matches()) {
            if (log.isDebugEnabled()) {
                log.debug("Matches basic pattern: {}", REGEX_BASIC);
            }
            matches = true;
            createHealthRuleEntry(healthRule, basic);
        } else {
            Matcher full;
            if ((full = this.fullPattern.matcher(str)).matches()) {
                if (log.isDebugEnabled()) {
                    log.debug("Matches full pattern: {}", REGEX_FULL);
                }
                matches = true;
                String op = full.group(1);
                HealthRule.RuleType type = HealthRule.RuleType.parse(op);
                if (type != null) {
                    healthRule.setRuleType(type);
                } else {
                    log.warn("Invalid rule type operator '{}', valid ones are: {}, just ignore it, using defaults!", new Object[]{op,
                            Arrays.toString((Object[]) HealthRule.RuleType.values()), healthRule.getRuleType()});
                }
                basic = this.basicPattern.matcher(str.substring(full.end(1)));
                while (basic.find())
                    createHealthRuleEntry(healthRule, basic);
            }
        }
        if (!matches) {
            throw new HealthRuleException(ErrorConstants.Message.MSG_INVALID_MONITOR_HEALTH_RULE_2, new Object[]{rule});
        }
        return healthRule;
    }

    private void createHealthRuleEntry(HealthRule healthRule, Matcher basic) {
        String op = basic.group(2);
        HealthRuleEntry.Operator operator = HealthRuleEntry.Operator.parse(op);
        if (operator == null) {
            throw new HealthRuleException(ErrorConstants.Message.MSG_INVALID_MONITOR_HEALTH_RULE_3, new Object[]{op, Arrays.toString((Object[]) HealthRuleEntry.Operator.values())});
        }
        String left = basic.group(1);
        String right = basic.group(3);
        healthRule.addRuleEntry(HealthRuleEntry.create(left, operator, right));
    }
}

