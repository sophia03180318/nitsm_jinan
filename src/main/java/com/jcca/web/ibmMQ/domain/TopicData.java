package com.jcca.web.ibmMQ.domain;


import com.jcca.web.ibmMQ.annotation.PCFParam;
import lombok.Data;

@Data
public class TopicData extends StatisticalData {
    private static final long serialVersionUID = 1L;

    public TopicData(Monitor monitor) {
        super(monitor);
    }

    @PCFParam(2092)
    private String topicName;

    @PCFParam(208)
    private TopicType topicType;

    @PCFParam(2094)
    private String topicString;

    @PCFParam(value = 215, option = 11)
    private int publishersCount;

    @PCFParam(value = 204, option = 11)
    private int subscribersCount;

    @PCFParam(option = 12)
    private int publishedMessagesCount;


    public enum TopicType {
        Local(0),

        Cluster(1);

        private final int value;

        TopicType(int value) {
            this.value = value;
        }

        public int getValue() {
            return this.value;
        }

    }

}


