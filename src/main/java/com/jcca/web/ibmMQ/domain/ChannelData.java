package com.jcca.web.ibmMQ.domain;

import com.jcca.web.ibmMQ.annotation.PCFParam;
import lombok.Data;

@Data
public class ChannelData extends StatisticalData {
    private static final long serialVersionUID = 1L;

    public ChannelData(Monitor monitor) {
        super(monitor);
    }

    @PCFParam(3501)
    private String channelName;

    @PCFParam(1511)
    private ChannelType channelType;

    @PCFParam(value = 1527, option = 11)
    private ChannelStatus channelStatus;

    @PCFParam(value = 1535, option = 11)
    private int bytesSent;

    @PCFParam(value = 1536, option = 11)
    private int bytesReceived;

    @PCFParam(value = 1538)
    private int buffersSent;

    @PCFParam(value = 1539)
    private int buffersReceived;

    private int messagesTransferred;


    public enum ChannelType {
        Sender(1), Server(2), Receiver(3), Requester(4), ServerConnection(7),
        ClientConnection(6), ClusterReceiver(8), ClusterSender(9);
        private final int value;

        ChannelType(int value) {
            this.value = value;
        }

        public int getValue() {
            return this.value;
        }

    }

    public enum ChannelTypeQuery {
        Sender(1), Server(2), Receiver(3), Requester(4);
        private final int value;

        ChannelTypeQuery(int value) {
            this.value = value;
        }

        public int getValue() {
            return this.value;
        }

    }

    public enum ChannelStatus {
        Stopped(6), Running(3), Stopping(4), Inactive(0), Initializing(13),
        Binding(1), Starting(2), Paused(8), Retrying(5),
        Requesting(7);

        private final int value;

        ChannelStatus(int value) {
            this.value = value;
        }

        public int getValue() {
            return this.value;
        }

    }

}
