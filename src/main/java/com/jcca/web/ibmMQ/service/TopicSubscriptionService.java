package com.jcca.web.ibmMQ.service;


import com.jcca.web.ibmMQ.common.ThreadFactories;
import com.jcca.web.ibmMQ.common.Time;
import com.jcca.web.ibmMQ.config.Configuration;
import com.jcca.web.ibmMQ.domain.Connection;
import com.jcca.web.ibmMQ.domain.Monitor;
import com.jcca.web.ibmMQ.domain.QueueData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class TopicSubscriptionService {
    private static final Logger log = LoggerFactory.getLogger(TopicSubscriptionService.class);
    private static final int EXECUTOR_MAXIMUM_POOL_SIZE = Math.min(10, Runtime.getRuntime().availableProcessors());
    private final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(EXECUTOR_MAXIMUM_POOL_SIZE,
            ThreadFactories.newDaemonThreadFactory("mq-monitoring-daemon-pool-sub-dest-q-cleaner"));

    private final Map<String, ScheduledFuture<?>> tasks = new ConcurrentHashMap<String, ScheduledFuture<?>>();
    private final IMetadataService metadataService;
    private long cleanInitialDelay = 60L;
    private long cleanInterval = 600L;
    private TimeUnit cleanTimeUnit = TimeUnit.SECONDS;

    public TopicSubscriptionService(IMetadataService metadataService, Configuration configuration) {
        this.metadataService = metadataService;
        try {
            Time time = Time.parseSMHD(configuration.getSubscriptionDestinationCleanInterval());
            this.cleanInterval = time.convertTo(this.cleanTimeUnit);
        } catch (Exception e) {
            log.warn(
                    String.format("Failed to parse subscriptionDestinationCleanInterval from config file, using defaults '%d' minutes.", new Object[]{

                            Long.valueOf(this.cleanInterval)
                    }), e);
        }
    }

    public void start(List<Connection> connections) {
        for (Connection connection : connections) {
            try {
                start(connection);
            } catch (Exception e) {
                log.error(String.format("Failed to start queue cleaner for '%s'.", new Object[]{connection.getName()}), e);
            }
        }
    }

    public void start(Connection connection) {
        String queueName = this.metadataService.querySubscriptionDestination(connection);
        scheduleQueueCleaner(connection, queueName);
    }

    public void stop(Connection connection) {
        unscheduleQueueCleaner(connection);
    }

    public void createDestinationQueue(Connection connection) {
        String queueName = this.metadataService.querySubscriptionDestination(connection);
        if (!this.metadataService.existObjectName(connection, Monitor.Category.Queue, queueName)) {
            if (log.isDebugEnabled()) {
                log.debug("Creating queue '{}' as destination for topic subscriptions.", queueName);
            }
            this.metadataService.createQueue(connection, queueName, QueueData.QueueType.Local);
            if (log.isDebugEnabled()) {
                log.debug("Queue '{}' is created.", queueName);
            }
        } else {
            log.warn("Queue '{}' as destination for topic subscriptions is already created, ready to use.", queueName);
        }
        scheduleQueueCleaner(connection, queueName);
    }

    public void deleteDestinationQueue(Connection connection) {
        String queueName = this.metadataService.querySubscriptionDestination(connection);
        if (log.isDebugEnabled()) {
            log.debug("Deleting queue '{}' for connection '{}'", queueName, connection.getName());
        }
        unscheduleQueueCleaner(connection);
        try {
            this.metadataService.deleteQueue(connection, queueName);
            if (log.isDebugEnabled()) {
                log.debug("Queue '{}' is deleted.", queueName);
            }
        } catch (Exception e) {
            if (log.isWarnEnabled()) {
                log.warn(String.format("Queue '%s' not exists for '%s'.", new Object[]{queueName, connection.getName()}), e);
            }
        }
    }

    public void shutdown() {
        this.scheduledExecutorService.shutdownNow();
        this.tasks.clear();
    }

    private boolean isScheduled(Connection connection) {
        return this.tasks.containsKey(connection.getName());
    }

    private void scheduleQueueCleaner(Connection connection, String queueName) {
        Connection clonedConnection = connection.clone();
        if (isScheduled(clonedConnection)) {
            if (log.isDebugEnabled()) {
                log.debug("Queue cleaner task for connection '{}' is already running!", clonedConnection.getName());
            }
            return;
        }
        if (log.isDebugEnabled()) {
            log.debug("Scheduling queue cleaner task for connection '{}'...", clonedConnection.getName());
        }
        ScheduledFuture<?> scheduledMonitorHandle = this.scheduledExecutorService.scheduleAtFixedRate(new QueueMessageCleaner(clonedConnection, queueName), this.cleanInitialDelay, this.cleanInterval, this.cleanTimeUnit);
        this.tasks.put(clonedConnection.getName(), scheduledMonitorHandle);
        if (log.isDebugEnabled()) {
            log.debug("Queue cleaner task is scheduled with scheduleId={} for connection '{}' successfully!",
                    clonedConnection.getId(), clonedConnection.getName());
        }
    }

    private void unscheduleQueueCleaner(Connection connection) {
        ScheduledFuture<?> scheduledMonitorHandle = this.tasks.remove(connection.getName());
        if (scheduledMonitorHandle == null) {
            if (log.isDebugEnabled()) {
                log.debug("No queue cleaner task is scheduled for connection '{}'!", connection.getName());
            }
            return;
        }
        if (log.isDebugEnabled()) {
            log.debug("Found scheduled queue cleaner task with scheduledId={} for connection '{}'!",
                    connection.getId(), connection.getName());
        }
        boolean cancelled = scheduledMonitorHandle.cancel(true);
        if (!cancelled) {
            log.error("Failed to cancel the queue cleaner task for connection '{}'!", connection.getName());
            return;
        }
        if (log.isDebugEnabled()) {
            log.debug("Queue cleaner task for connection '{}' is unscheduled successfully!", connection.getName());
        }
    }

    class QueueMessageCleaner
            implements Runnable {
        private final Connection connection;
        private final String queueName;

        public QueueMessageCleaner(Connection connection, String queueName) {
            this.connection = connection;
            this.queueName = queueName;
        }

        public void run() {
            try {
            } catch (Exception e) {
                log.error("Error when clearing queue " + this.queueName, e);
            }
        }
    }
}