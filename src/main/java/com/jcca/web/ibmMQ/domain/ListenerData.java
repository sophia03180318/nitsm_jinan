package com.jcca.web.ibmMQ.domain;


import com.jcca.web.ibmMQ.annotation.PCFParam;
import lombok.Data;

@Data
public class ListenerData extends StatisticalData {
    private static final long serialVersionUID = 1L;

    public ListenerData(Monitor monitor) {
        super(monitor);
    }

    @PCFParam(3554)
    private String listenerName;

    @PCFParam(3552)
    private String listenerAddress;

    @PCFParam(1522)
    private int listenerPort;

    @PCFParam(1602)
    private int backlog;
    @PCFParam(value = 1599, option = 11)
    private ListenerData.ListenerStatus listenerStatus;

    @PCFParam(1501)
    private TransportType transportType;


    public enum ListenerStatus {
        Starting(1), Running(2), Stopping(3),

        Stopped(-1);

        private final int value;


        ListenerStatus(int value) {
            this.value = value;
        }


        public int getValue() {
            return this.value;
        }
    }

    public enum TransportType {
        TCP(2), LU62(1), NetBIOS(3), SPX(4);
        private final int value;

        TransportType(int value) {
            this.value = value;
        }

        public int getValue() {
            return this.value;
        }
    }
}

