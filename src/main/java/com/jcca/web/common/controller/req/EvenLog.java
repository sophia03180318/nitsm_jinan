package com.jcca.web.common.controller.req;

import lombok.Data;

/**
 * @ Author：sophia
 * @ Date：Created in 19:35 2023/3/15
 * @ Description:
 */
@Data
public class EvenLog {
    private static final long serialVersionUID = 1;
    private long sequenceNumber;
    private int eventType;
    private long timeStamp;
    private String data;
    private String id;

    @Override
    public String toString() {
        return "EvenLog{" +
                "sequenceNumber=" + sequenceNumber +
                ", eventType=" + eventType +
                ", timeStamp=" + timeStamp +
                ", data='" + data + '\'' +
                ", id='" + id + '\'' +
                '}';
    }
}
