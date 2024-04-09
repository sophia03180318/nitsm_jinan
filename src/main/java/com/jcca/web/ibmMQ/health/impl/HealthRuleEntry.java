package com.jcca.web.ibmMQ.health.impl;


import javax.annotation.concurrent.Immutable;


@Immutable
final class HealthRuleEntry {
    private final String left;
    private final Object right;
    private final Operator operator;


    private HealthRuleEntry(String left, Operator operator, Object right) {

        this.left = left;

        this.operator = operator;

        this.right = right;

    }

    public static HealthRuleEntry create(String left, Operator operator, Object right) {
        return new HealthRuleEntry(left, operator, right);
    }


    public String getLeft() {
        return this.left;
    }


    public Object getRight() {
        return this.right;
    }


    public Operator getOperator() {
        return this.operator;
    }


    public String toEvaluableString() {

        String op = (this.operator == Operator.EQ) ? "==" : this.operator.op;

        Object value = isNumeric(String.valueOf(this.right)) ? this.right : ("'" + this.right + "'");
        return "(" + this.left + op + value + ")";
    }


    private static boolean isNumeric(String str) {
        try {

            Double.parseDouble(str);

        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;

    }

    public String toExpression() {
        return this.left + this.operator.op + this.right;
    }

    public String toString() {
        return "(" + toExpression() + ")";
    }


    public enum Operator {
        EQ("="), NE("!="), GT(">"), GE(">="), LT("<"), LE("<=");
        private final String op;

        Operator(String op) {
            this.op = op;
        }


        public String getValue() {
            return this.op;
        }

        public static Operator parse(String op) {
            for (Operator operator : values()) {
                if (operator.op.equals(op)) {
                    return operator;
                }
            }
            return null;
        }
    }
}

