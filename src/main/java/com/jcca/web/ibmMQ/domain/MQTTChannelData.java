package com.jcca.web.ibmMQ.domain;

import com.jcca.web.ibmMQ.annotation.PCFParam;
import lombok.Data;

@Data
public class MQTTChannelData {
    private static final long serialVersionUID = 1L;
    private int id;

    private int connectionId;

    @PCFParam(3501)
    private String channelName;

    @PCFParam(3564)
    private String clientId;

    @PCFParam(1527)
    private ChannelStatus channelStatus;

    @PCFParam(3506)
    private String connectionName;

    @PCFParam(3525)
    private String lastMessageDate;


    @PCFParam(3524)
    private String lastMessageTime;

    @PCFParam(1634)
    private long messagesReceived;


    @PCFParam(1633)
    private long messagesSent;

    @PCFParam(1635)
    private int pendingOutbound;

    @PCFParam(1566)
    private int keepAliveInterval;

    public enum ChannelStatus {
        Disconnected(9),

        Running(3);

        private final int value;

        ChannelStatus(int value) {
            this.value = value;
        }

        public int getValue() {
            return this.value;
        }
    }
}

