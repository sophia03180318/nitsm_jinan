package com.jcca.web.ibmMQ.health;


import com.jcca.web.ibmMQ.domain.HealthMessage;
import com.jcca.web.ibmMQ.vo.HealthState;

import java.util.ArrayList;
import java.util.List;


public final class HealthResult {
    private HealthState healthState = HealthState.OK;
    private List<HealthMessage> messages = new ArrayList<>();
    public static final HealthResult OK = new HealthResult(HealthState.OK);


    public HealthResult() {
    }


    public HealthResult(HealthState healthState) {
        this.healthState = healthState;
    }

    public HealthState getHealthState() {
        return this.healthState;
    }

    public void setHealthState(HealthState healthState) {
        this.healthState = healthState;
    }


    public List<HealthMessage> getMessages() {
        return this.messages;
    }


    public void addMessage(HealthMessage message) {
        this.messages.add(message);
    }


    public HealthResult combine(HealthResult another) {

        if (another == null) {

            return this;

        }

        int result = this.healthState.compareTo((HealthState) another.healthState);

        if (result < 0) {

            return another;

        }

        return this;

    }

    public String toString() {
        return this.healthState.toString();
    }
}
