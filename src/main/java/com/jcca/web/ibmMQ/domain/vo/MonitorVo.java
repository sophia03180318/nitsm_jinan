package com.jcca.web.ibmMQ.domain.vo;


import com.jcca.web.ibmMQ.domain.Monitor;
import lombok.Data;

@Data
public class MonitorVo {
    private static final long serialVersionUID = 1L;
    private String id;
    private String name;
    private Monitor.Category category;
    private String objectType;
    private String objectName;
    private Monitor.ViewType viewType;
    private Monitor.State state;
    private String pollingInterval;
    private String dataExpirationTime;
    private String measurements;
    private String healthRule;
    private Monitor.Scope scope;
    private String description;
    private String connectionName;
    private String Usage;
    private String GroupId;


}

