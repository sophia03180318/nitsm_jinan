package com.jcca.web.ibmMQ.schedule.impl;


import com.jcca.web.ibmMQ.common.MonitoringException;
import com.jcca.web.ibmMQ.domain.Monitor;
import com.jcca.web.ibmMQ.schedule.IMonitorScheduleStrategy;
import com.jcca.web.ibmMQ.service.ConnectionService;
import com.jcca.web.ibmMQ.service.IMetadataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.annotation.concurrent.ThreadSafe;

@Component("simpleScheduleStrategy")
@ThreadSafe
public class SimpleMonitorScheduleStrategy implements IMonitorScheduleStrategy {
    private static final Logger log = LoggerFactory.getLogger(SimpleMonitorScheduleStrategy.class);
    @Resource(name = "metadataService")
    private IMetadataService metadataService;

    @Resource
    private ConnectionService connectionService;

    public String getName() {
        return "default";
    }

    public boolean canSchedule(Monitor monitor) {
        if (monitor.getCategory() == Monitor.Category.QueueManager) {
            return true;
        }
        if (monitor.isMQTT()) {
            return true;
        }
        try {
            if (!this.metadataService.existObjectName(monitor.getConnection(), monitor.getCategory(), monitor.getObjectName())) {
                log.error(String.format("MQ Object with name '%s' not exists in connection '%s'!", monitor.getObjectName(), monitor.getConnection().getName()));
                return false;
            }
            return true;
        } catch (MonitoringException e) {
            log.error(String.format("Failed to check object name existence for monitor '%s' in connection '%s'!", monitor.getName(), monitor.getConnection().getName()));

            return false;

        }

    }
}

