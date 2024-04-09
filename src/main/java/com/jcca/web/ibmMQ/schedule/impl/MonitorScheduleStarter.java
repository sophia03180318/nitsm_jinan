package com.jcca.web.ibmMQ.schedule.impl;


import com.jcca.web.ibmMQ.common.ThreadFactories;
import com.jcca.web.ibmMQ.common.Time;
import com.jcca.web.ibmMQ.config.Configuration;
import com.jcca.web.ibmMQ.domain.Connection;
import com.jcca.web.ibmMQ.domain.Monitor;
import com.jcca.web.ibmMQ.entity.IBMMonitor;
import com.jcca.web.ibmMQ.schedule.IMonitorScheduler;
import com.jcca.web.ibmMQ.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


@Component
public class MonitorScheduleStarter {
    private static final Logger log = LoggerFactory.getLogger(MonitorScheduleStarter.class);
    private static final int DEFAULT_DATA_PURGE_INITIAL_DELAY = 1;
    private static final int DEFAULT_DATA_PURGE_INTERVAL = 30;
    private static final int DEFAULT_MQTT_CONNECTION_REFRESH_INITIAL_DELAY = 30;
    private static final int DEFAULT_MQTT_CONNECTION_REFRESH_INTERVAL = 30;
    private static final int EXECUTOR_MAXIMUM_POOL_SIZE = 3;
    private final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(3,
            ThreadFactories.newDaemonThreadFactory());

    @Resource(name = "configuration")
    private Configuration configuration;
    @Resource
    private StatisticalDataService statisticalDataManager;
    @Resource
    private ConnectionService connectionManager;
    @Resource
    private MonitorService monitorManager;
    @Resource(name = "monitorScheduler")
    private IMonitorScheduler monitorScheduler;
    @Resource(name = "metadataService")
    private IMetadataService metadataService;
    @Resource
    private ChannelDataService mqttChannelManager;
    @Resource(name = "messageService")
    private PCFMessageService messageService;


    @PostConstruct
    private void init() {
        startDataPurgeTask();
        if (this.configuration.isConnectionAutoStart()) {
            startMonitorTaskRescheduler();
        }
    }

    private void startMonitorTaskRescheduler() {
        this.scheduledExecutorService.schedule(new MonitorTaskRescheduler(), 1000L, TimeUnit.MILLISECONDS);
    }


    private void startDataPurgeTask() {
        String dataPurgeInterval = this.configuration.getDataPurgeInterval();
        long initialDelay = 1L;
        long interval = 30L;
        TimeUnit unit = TimeUnit.MINUTES;
        try {
            Time time = Time.parseMHD(dataPurgeInterval);
            interval = time.convertTo(unit);
        } catch (Exception e) {
            log.warn(String.format("Failed to parse dataPurgeInterval from config file, using defaults '%d' minutes.", interval));
        }
        //数据清除   调度任务
        this.scheduledExecutorService.scheduleWithFixedDelay(new DataPurgeTaskRunner(), initialDelay, interval, unit);
    }

    @PreDestroy
    private void shutdown() {
        this.scheduledExecutorService.shutdownNow();
    }

    class MonitorTaskRescheduler implements Runnable {
        public void run() {

            log.info("Resuming scheduled monitor tasks...");

            int resumedOkCount = 0;
            int resumedFailCount = 0;
            List<Connection> connections = MonitorScheduleStarter.this.connectionManager.listConnections();
            for (Connection connection : connections) {
                try {
                    MonitorScheduleStarter.this.messageService.connect(connection);
                } catch (Exception ex) {
                    log.error(String.format("Failed to connect to QM with '%s'", connection.getName()), ex.getMessage());
                }

                String connectionName = connection.getName();

                for (IBMMonitor ibmMonitor : MonitorScheduleStarter.this.monitorManager.listMonitors(connectionName, Monitor.State.Active.getValue(), null)) {
                    Monitor monitor = Monitor.getMonitor(ibmMonitor);
                    try {
                        if (log.isDebugEnabled()) {
                            log.debug("Found active monitor '{}'.", monitor.getName());
                        }
                        MonitorScheduleStarter.this.monitorScheduler.scheduleMonitor(monitor);
                        resumedOkCount++;
                        if (log.isDebugEnabled()) {
                            log.debug("Monitor tasks '{}' rescheduled ok.", monitor.getName());
                        }
                    } catch (Exception e) {
                        resumedFailCount++;
                        log.error(String.format("Failed to reschedule monitor '%s', cause: '%s'!", monitor.getName(), e.getMessage()));
//                        if (!monitor.isDefault()) {去掉改变状态标识
//                            failMonitorInactive(connectionName, monitor);
//                        }
                    }
                }
            }
            if (log.isInfoEnabled()) {
                log.info("Monitors resuming result: [ total:{}, success:{}, failed:{} ].", resumedOkCount + resumedFailCount, resumedOkCount, resumedFailCount);
            }

        }


        private void failMonitorInactive(String connectionName, Monitor monitor) {
            try {
                MonitorScheduleStarter.this.monitorManager.updateMonitorState(connectionName, monitor.getName(), Monitor.State.Inactive);
            } catch (Exception ex) {
                log.error(String.format("Failed to set monitor '%s' state to inactive, cause: '%s'!", monitor.getName(), ex.getMessage()));
            }
        }
    }

    class DataPurgeTaskRunner implements Runnable {
        public void run() {
            try {
                if (log.isDebugEnabled()) {
                    log.debug("Begin to clean up exipred data ....");
                }
                List<Connection> connections = MonitorScheduleStarter.this.connectionManager.listConnections();
                for (Connection connection : connections) {
                    for (IBMMonitor IBMMonitor : MonitorScheduleStarter.this.monitorManager.listMonitors(connection.getName(), null, null)) {
                        Monitor monitor = Monitor.getMonitor(IBMMonitor);
                        try {
                            MonitorScheduleStarter.this.statisticalDataManager.removeExpiredStatistics(monitor);
                            if (log.isDebugEnabled()) {
                                log.debug("Cleaned exipred data for monitor '{}' in connection '{}'", monitor.getName(), connection.getName());
                            }
                        } catch (Exception e) {
                            log.error(String.format("Failed remove expired statistics for monitor '%s'", monitor.getName()));
                        }
                    }
                }
                if (log.isDebugEnabled()) {
                    log.debug("End of exipred data cleaning for {} connections.", connections.size());
                }
            } catch (Exception e) {
                log.error("Unexpected error when performing data purge task!{}", e.getMessage());
            }
        }
    }
    //mqtt的内容先不做监控
//    class MQTTChannelDataCollector implements Runnable {
//        public void run() {
//            try {
//                if (log.isDebugEnabled()) {
//                    log.debug("Begin to collecting MQTT channel data ....");
//                }
//                List<Connection> connections = MonitorScheduleStarter.this.connectionManager.listConnections();
//                for (Connection connection : connections) {
//                    try {
//                        if (!MonitorScheduleStarter.this.messageService.isConnected(connection)) {
//                            return;
//                        }
//                        if (!MonitorScheduleStarter.this.metadataService.isQMgrAvailable(connection)) {
//                            if (log.isDebugEnabled()) {
//                                log.debug("QMgr is not available for connection '{}', skip collecting data for this connection.", connection.getName());
//                            }
//                            continue;
//                        }
//                        if (!MonitorScheduleStarter.this.metadataService.isMQTTServiceAvailable(connection)) {
//                            if (log.isDebugEnabled()) {
//                                log.debug("MQTT service is not available for connection '{}', skip collecting data for this connection.", connection.getName());
//                            }
//                            continue;
//                        }
//                        List<MQTTChannelData> dataList = MonitorScheduleStarter.this.metadataService.queryMQTTChannelStatistics(connection);
//                        if (log.isDebugEnabled()) {
//                            log.debug("Found {} clients connected in connection '{}'!", Integer.valueOf(dataList.size()), connection.getName());
//                        }
//                        MonitorScheduleStarter.this.mqttChannelManager.saveChannelData(connection, dataList);
//                    } catch (Exception e) {
//                        log.error(String.format("Unexpected error when collecting MQTT channel data for connection '%s'!", new Object[]{connection.getName()}), e);
//                    }
//                }
//                if (log.isDebugEnabled()) {
//                    log.debug("End of collecting MQTT channel data!");
//                }
//            } catch (Exception e) {
//                log.error("Unexpected error when collecting  MQTT channel data!", e);
//            }
//        }
//    }
}