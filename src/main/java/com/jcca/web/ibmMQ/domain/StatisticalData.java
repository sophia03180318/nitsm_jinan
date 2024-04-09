package com.jcca.web.ibmMQ.domain;

import com.jcca.web.ibmMQ.vo.HealthState;
import lombok.Data;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
public abstract class StatisticalData {
    private static final long serialVersionUID = 1L;
    private int id;
    private Date captureTime;
    private HealthState healthState;
    private Set<HealthMessage> messages;
    private String typeString;
    private Monitor monitor;

    public StatisticalData() {
        super();
    }

    public StatisticalData(Monitor monitor) {
        this.monitor = monitor;
    }

    public <A> A adapt(Class<A> type) {
        if ((type == ChannelData.class && this instanceof ChannelData)
                || (type == QueueData.class && this instanceof QueueData)
                || (type == TopicData.class && this instanceof TopicData)
                || (type == ListenerData.class && this instanceof ListenerData)
                || (type == QMgrData.class && this instanceof QMgrData)
                || type == StatisticalData.class) {


            return (A) this;
        }
        return null;
    }

    public void addMessages(List<HealthMessage> allMessages) {
        if (allMessages.size() == 0) {
            return;
        }
        for (HealthMessage message : allMessages) {
            addMessage(message);
        }
    }

    public void addMessage(HealthMessage message) {
        if (this.messages == null) {
            this.messages = new HashSet();
        }
        this.messages.add(message);
    }


}


