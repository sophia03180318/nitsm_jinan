package com.jcca.web.ibmMQ.vo;


public class TopicMQObject extends MQObject {

    private String topicString;

    protected TopicMQObject() {
    }

    public TopicMQObject(String category, String name, String type, String topicString) {
        super(category, name, type);
        this.topicString = topicString;
    }

    public String getTopicString() {
        return this.topicString;
    }

    public String toString() {
        return getClass().getSimpleName() + " [" + baseString() + "," + "topicString=" +
                this.topicString + "]";
    }
}


