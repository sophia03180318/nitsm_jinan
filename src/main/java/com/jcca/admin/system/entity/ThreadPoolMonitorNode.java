package com.jcca.admin.system.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class ThreadPoolMonitorNode implements Serializable {
    private String poolName;
    private Integer poolSize;
    private Integer corePoolSize;
    private Long completedTaskCount;
    private Long taskCount;
    private Integer activeCount;
    private Integer queueSize;
    private Integer largestPoolSize;
    private Integer maximumPoolSize;
    private Long keepAliveTime;
    private boolean shutdown;
    private boolean terminated;
}
