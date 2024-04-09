package com.jcca.web.ibmMQ.vo;

public enum HealthState {
    OK("OK"), WARN("WARN"), ERROR("ERROR"), UNKNOWN("UNKNOWN");
    private final String value;

    HealthState(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }
}
