package com.jcca.web.ibmMQ.vo;


public class MQObject {
    private String category;
    private String type;
    private String name;

    protected MQObject() {
    }

    public MQObject(String category, String name, String type) {
        this.category = category;
        this.name = name;
        this.type = type;

    }

    public String getCategory() {
        return this.category;
    }


    public String getType() {
        return this.type;
    }

    public String getName() {
        return this.name;
    }


    protected String baseString() {
        return "category=" + this.category + "," + "name=" + this.name + "," +
                "type=" + this.type;
    }

    public String toString() {
        return getClass().getSimpleName() + " [" + baseString() + "]";
    }
}
