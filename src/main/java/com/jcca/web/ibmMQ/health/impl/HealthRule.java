package com.jcca.web.ibmMQ.health.impl;


import com.jcca.web.ibmMQ.vo.HealthState;

import java.util.ArrayList;
import java.util.List;


final class HealthRule implements Comparable<HealthRule> {
    private List<HealthRuleEntry> entries = new ArrayList<HealthRuleEntry>();

    private RuleType ruleType;
    private HealthState targetHealthState;

    public HealthRule(HealthState targetHealthState) {
        this(RuleType.MATCH_SINGLE, targetHealthState);
    }


    public HealthRule(RuleType ruleType, HealthState targetHealthState) {
        this.ruleType = ruleType;
        this.targetHealthState = targetHealthState;
    }

    public HealthState getTargetHealthState() {
        return this.targetHealthState;
    }


    public List<HealthRuleEntry> getEntries() {
        return this.entries;
    }


    public RuleType getRuleType() {
        return this.ruleType;
    }

    public void setRuleType(RuleType ruleType) {
        this.ruleType = ruleType;
    }


    public void addRuleEntry(HealthRuleEntry ruleEntry) {
        this.entries.add(ruleEntry);
    }


    private String buildRule() {
        StringBuffer workingBuffer = new StringBuffer(this.targetHealthState.name());
        if (this.ruleType != RuleType.MATCH_SINGLE) {
            workingBuffer.append("(");
            workingBuffer.append(this.ruleType.getOperator());

        }

        for (HealthRuleEntry entry : this.entries) {
            workingBuffer.append(entry);
        }
        if (this.ruleType != RuleType.MATCH_SINGLE) {
            workingBuffer.append(")");
        }
        return workingBuffer.toString().trim();
    }


    public int compareTo(HealthRule anotherRule) {
        if (anotherRule == null) {
            return 1;
        }
        return this.targetHealthState.compareTo(anotherRule.getTargetHealthState());
    }


    public int hashCode() {
        return buildRule().hashCode();
    }

    public boolean equals(Object obj) {
        if (this == obj)
            return true;

        if (obj == null)
            return false;

        if (getClass() != obj.getClass())
            return false;

        return obj.toString().equals(toString());
    }


    public String toString() {
        return buildRule();
    }

    public enum RuleType {
        MATCH_ALL("&", "Match All"), MATCH_ANY("|", "Match Any"), MATCH_SINGLE("", "Match Single");
        private final String operator;
        private final String label;


        RuleType(String operator, String label) {

            this.operator = operator;

            this.label = label;
        }


        public String getLabel() {
            return this.label;
        }


        public String getOperator() {
            return this.operator;
        }

        public static RuleType parse(String op) {

            for (RuleType rule : values()) {

                if (rule.operator.equals(op)) {

                    return rule;

                }

            }

            return null;
        }
    }
}

