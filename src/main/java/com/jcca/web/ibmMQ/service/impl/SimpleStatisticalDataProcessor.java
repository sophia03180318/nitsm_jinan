package com.jcca.web.ibmMQ.service.impl;


import com.jcca.web.ibmMQ.common.ThreadFactories;
import com.jcca.web.ibmMQ.domain.StatisticalData;
import com.jcca.web.ibmMQ.notification.INotificationService;
import com.jcca.web.ibmMQ.service.StatisticalDataService;
import com.jcca.web.ibmMQ.support.IStatisticalDataProcessor;
import com.jcca.web.ibmMQ.vo.HealthState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;


@Component("simpleStatisticalDataProcessor")
public class SimpleStatisticalDataProcessor implements IStatisticalDataProcessor {
    private static final Logger log = LoggerFactory.getLogger(SimpleStatisticalDataProcessor.class);

    private ExecutorService executorService;


    @Resource
    private StatisticalDataService statisticalDataManager;

    @Resource(name = "notificationService")
    private INotificationService notificationService;


    @PostConstruct
    public void start() {

        this.executorService = Executors.newCachedThreadPool(ThreadFactories.newDaemonThreadFactory("mq-monitoring-persistence-worker-pool"));

    }

    public void process(StatisticalData data) {

        if (log.isDebugEnabled()) {

            log.debug("Processing statistical data {}...", data);

        }

        try {

            this.executorService.submit(new DataPersistenceWorker(this.statisticalDataManager, data));

        } catch (Exception e) {

            log.error("Got error when run DataPersistenceWorker", e);
        } finally {
            if (data.getHealthState() != HealthState.OK) {

                this.notificationService.notify(data);
            }
        }
    }


    @PreDestroy
    public void stop() {

        if (!this.executorService.isTerminated()) {

            this.executorService.shutdownNow();

        }

    }

    static class DataPersistenceWorker implements Runnable {
        private static final AtomicInteger workerIndex = new AtomicInteger(1);

        private final String name;

        private final StatisticalData data;

        private final StatisticalDataService statisticalDataManager;

        public DataPersistenceWorker(StatisticalDataService statisticalDataManager, StatisticalData data) {
            this.statisticalDataManager = statisticalDataManager;
            this.data = data;
            this.name = String.format("%s-%d", new Object[]{getClass().getSimpleName(), Integer.valueOf(workerIndex.getAndIncrement())});
        }

        public void run() {
            if (log.isDebugEnabled()) {
                log.debug("'{}' saving statistical data '{}' to database...", this.name, this.data);
            }
            try {
                this.statisticalDataManager.saveStatisticalData(this.data);
            } catch (Exception e) {
                log.error(String.format("%s failed to save data '%s' to database!", new Object[]{this.name, this.data}), e);
            }
        }

        public String toString() {
            return this.name;
        }
    }
}