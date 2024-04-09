package com.jcca.web.ibmMQ.domain;


import com.jcca.web.ibmMQ.entity.IBMMonitor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Monitor implements Cloneable {
    private static final long serialVersionUID = 1L;
    public static final String DEFAULT_MONITOR_NAME = "qmgr";

    private String id;
    private String name;
    private Category category;
    private String objectType;
    private String objectName;
    private ViewType viewType;
    private State state;
    private String pollingInterval;
    private String dataExpirationTime;
    private List<String> measurements = new ArrayList<>();
    private String healthRule;
    private Scope scope;
    private String description;
    private Connection connection;
    private String Usage;
    private String groupId;
    private int value;


    public Monitor(Connection connection) {
        this.connection = connection;
    }

    public Monitor() {
        super();
    }


    public Monitor clone() {
        try {
            Monitor monitor = (Monitor) super.clone();
            return monitor;
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }


    public enum Category {
        QueueManager("QueueManager"), Channel("Channel"), Queue("Queue"), Topic("Topic"), Listener("Listener");
        private final String value;

        Category(String value) {
            this.value = value;
        }

        public String getValue() {
            return this.value;
        }
    }

    public enum CategoryQuery {
        Channel("Channel"), Queue("Queue");
        private final String value;

        CategoryQuery(String value) {
            this.value = value;
        }

        public String getValue() {
            return this.value;
        }
    }


    public enum MonitorNameType {
        QMGR("qmgr");
        private final String value;

        MonitorNameType(String value) {
            this.value = value;
        }

        public String getValue() {
            return this.value;
        }
    }

    public enum ViewType {
        LineChart("LineChart"), BarChart("BarChart"), PieChart("PieChart"), AreaChart("AreaChart"), Table("Table");
        private final String value;

        ViewType(String value) {
            this.value = value;
        }

        public String getValue() {
            return this.value;
        }
    }

    public enum State {
        Active("Active"), Inactive("Inactive");
        private final String value;

        State(String value) {
            this.value = value;
        }

        public String getValue() {
            return this.value;
        }
    }

    public enum Scope {
        SYSTEM("SYSTEM"), NORMAL("NORMAL"), MQTT("MQTT");
        private final String value;

        Scope(String value) {
            this.value = value;
        }

        public String getValue() {
            return this.value;
        }
    }

    /**
     * 是否为mqtt
     */
    public boolean isMQTT() {
        return this.scope == Scope.MQTT;
    }

    /**
     * 是否为默认队列
     */
    public boolean isDefault() {
        return "qmgr".equalsIgnoreCase(this.name);
    }

    /**
     * 是否为活动状态
     */

    public boolean isActive() {
        return this.state == Monitor.State.Active;
    }

    public static Monitor getMonitor(IBMMonitor ibmMonitor) {
        Monitor monitor = new Monitor();
        monitor.setId(ibmMonitor.getId());
        monitor.setName(ibmMonitor.getName());
        monitor.setGroupId(ibmMonitor.getGroupId());
        switch (ibmMonitor.getCategory()) {
            case "QueueManager":
                monitor.setCategory(Monitor.Category.QueueManager);
                break;
            case "Channel":
                monitor.setCategory(Monitor.Category.Channel);
                monitor.addMeasurement("bytesReceived");
                monitor.addMeasurement("bytesSent");
                monitor.addMeasurement("channelStatus");
                break;
            case "Queue":
                monitor.setCategory(Monitor.Category.Queue);
                if (ibmMonitor.getObjectType().equals("Local")) {
                    monitor.addMeasurement("currentQDepth");
                    monitor.addMeasurement("openInputCount");
                    monitor.addMeasurement("openOutputCount");
                    monitor.addMeasurement("maxQDepth");
                    monitor.addMeasurement("occupiedPercent");
                } else if (ibmMonitor.getObjectType().equals("Remote")) {
                    monitor.addMeasurement("occupiedPercent");
                }
                break;
            case "Topic":
                monitor.setCategory(Monitor.Category.Topic);
                break;
            case "Listener":
                monitor.setCategory(Monitor.Category.Listener);
                break;
        }

        monitor.setObjectType(ibmMonitor.getObjectType());
        monitor.setObjectName(ibmMonitor.getObjectName());
        switch (ibmMonitor.getViewType()) {
            case "LineChart":
                monitor.setViewType(Monitor.ViewType.LineChart);
                break;
            case "BarChart":
                monitor.setViewType(Monitor.ViewType.BarChart);
                break;
            case "PieChart":
                monitor.setViewType(Monitor.ViewType.PieChart);
                break;
            case "AreaChart":
                monitor.setViewType(Monitor.ViewType.AreaChart);
                break;
            case "Table":
                monitor.setViewType(Monitor.ViewType.Table);
                break;
        }
        //  Active("Active"), Inactive("Inactive");
        switch (ibmMonitor.getState()) {
            case "Active":
                monitor.setState(Monitor.State.Active);
                break;
            case "Inactive":
                monitor.setState(Monitor.State.Inactive);
                break;
        }

        monitor.setPollingInterval(ibmMonitor.getPollingInterval());
        monitor.setDataExpirationTime(ibmMonitor.getDataExpirationTime());
        monitor.setHealthRule(ibmMonitor.getHealthRule());
        switch (ibmMonitor.getScope()) {
            case "SYSTEM":
                monitor.setScope(Monitor.Scope.SYSTEM);
                break;
            case "NORMAL":
                monitor.setScope(Monitor.Scope.NORMAL);
                break;
            case "MQTT":
                monitor.setScope(Monitor.Scope.MQTT);
                break;

        }

        monitor.setDescription(ibmMonitor.getDescription());
        Connection connection = new Connection();
        connection.setId(ibmMonitor.getConnectionId());
        connection.setName(ibmMonitor.getConnectionName());
        connection.setChannelName(ibmMonitor.getChannelName());
        connection.setHost(ibmMonitor.getHost());
        connection.setPort(ibmMonitor.getPort());
        connection.setUserId(ibmMonitor.getUserId());
        connection.setDescription(ibmMonitor.getConnectionDescription());
        monitor.setConnection(connection);
        return monitor;
    }

    public static IBMMonitor getIBMMonitor(Monitor monitor) {
        IBMMonitor ibmMonitor = new IBMMonitor();
        ibmMonitor.setId(monitor.getId());
        ibmMonitor.setCategory(monitor.getCategory().getValue());
        ibmMonitor.setDataExpirationTime(monitor.getDataExpirationTime());
        ibmMonitor.setDescription(monitor.getDescription());
        ibmMonitor.setHealthRule(monitor.getHealthRule());
        ibmMonitor.setName(monitor.getName());
        ibmMonitor.setObjectName(monitor.getObjectName());
        ibmMonitor.setObjectType(monitor.getObjectType());
        ibmMonitor.setUsage(monitor.getUsage());
        ibmMonitor.setGroupId(monitor.getGroupId());
        ibmMonitor.setPollingInterval(monitor.getPollingInterval());
        ibmMonitor.setScope(monitor.getScope().getValue());
        ibmMonitor.setState(monitor.getState().getValue());
        ibmMonitor.setMeasurement(monitor.getMeasurements().get(0));
        ibmMonitor.setViewType(monitor.getViewType().getValue());
        ibmMonitor.setConnectionId(monitor.getConnection().getId());
        return ibmMonitor;
    }

    public void addMeasurement(String measurements) {
        this.measurements.add(measurements);

    }

    public boolean isTopicCategory() {
        return this.category == Monitor.Category.Topic;
    }
}

