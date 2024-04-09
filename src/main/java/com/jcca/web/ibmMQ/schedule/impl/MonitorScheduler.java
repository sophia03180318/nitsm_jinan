package com.jcca.web.ibmMQ.schedule.impl;


import com.jcca.web.ibmMQ.command.ICommandProcessor;
import com.jcca.web.ibmMQ.common.ErrorConstants;
import com.jcca.web.ibmMQ.common.ThreadFactories;
import com.jcca.web.ibmMQ.common.Time;
import com.jcca.web.ibmMQ.config.Configuration;
import com.jcca.web.ibmMQ.diagnose.IDiagnosable;
import com.jcca.web.ibmMQ.diagnose.IDiagnoseStrategy;
import com.jcca.web.ibmMQ.domain.Monitor;
import com.jcca.web.ibmMQ.schedule.*;
import com.jcca.web.ibmMQ.support.IStatisticalDataProcessor;
import com.jcca.web.ibmMQ.util.ServiceLocator;
import com.jcca.web.ibmMQ.util.concurrent.Executors;
import com.jcca.web.ibmMQ.util.concurrent.ITrackableScheduledExecutorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;


@Component("monitorScheduler")
public class MonitorScheduler implements IMonitorScheduler {
    private static final Logger log = LoggerFactory.getLogger(MonitorScheduler.class);

    private static final int EXECUTOR_DEFAULT_POOL_SIZE = Math.min(10, Runtime.getRuntime().availableProcessors());
    private static final int DEFAULT_DIAGNOSE_INITIAL_DELAY = 60;
    private static final int DEFAULT_DIAGNOSE_INTERVAL = 10;
    private final Map<String, ScheduledFuture<?>> tasks = new ConcurrentHashMap<String, ScheduledFuture<?>>();
    private final Map<String, IDiagnosable> diagnosedTasks = new ConcurrentHashMap<String, IDiagnosable>();


    private ITrackableScheduledExecutorService scheduledExecutorService;


    @Resource(name = "simpleStatisticalDataProcessor")
    private IStatisticalDataProcessor statisticalDataProcessor;


    @Resource(name = "configuration")
    private Configuration configuration;

    @Resource(name = "commandProcessor")
    private ICommandProcessor commandProcessor;


    @Resource(name = "simpleDiagnoseStrategy")
    private IDiagnoseStrategy diagnoseStrategy;


    @Resource(name = "simpleScheduleStrategy")
    private IMonitorScheduleStrategy monitorScheduleStrategy;


    @PostConstruct
    private void init() {
        //配置当前线程个数
        int maxinumConcurrentTasks = this.configuration.getMaxActiveMonitors();
        if (maxinumConcurrentTasks < EXECUTOR_DEFAULT_POOL_SIZE) {
            maxinumConcurrentTasks = EXECUTOR_DEFAULT_POOL_SIZE;
        }
        if (log.isDebugEnabled()) {
            log.debug("maxinumConcurrentTasks {}", Integer.valueOf(maxinumConcurrentTasks));
        }
        this.scheduledExecutorService = Executors.newTrackableScheduledThreadPool(maxinumConcurrentTasks,
                ThreadFactories.newDaemonThreadFactory("mq-monitoring-daemon-pool-scheduler"));
        Map<String, IDiagnoseStrategy> diagnoseStrategies = ServiceLocator.findServices(IDiagnoseStrategy.class);
        String diagnoseStrategyName = this.configuration.getMonitorDiagnoseStrategy();

        if (diagnoseStrategyName != null && diagnoseStrategyName != "default") {
            IDiagnoseStrategy strategy = diagnoseStrategies.get(diagnoseStrategyName);
            if (strategy != null) {
                this.diagnoseStrategy = strategy;
            }
        }

        Map<String, IMonitorScheduleStrategy> scheduleStrategies = ServiceLocator.findServices(IMonitorScheduleStrategy.class);

        String scheduleStrategyName = this.configuration.getMonitorScheduleStrategy();

        if (scheduleStrategyName != null && diagnoseStrategyName != "default") {

            IMonitorScheduleStrategy strategy = scheduleStrategies.get(scheduleStrategyName);

            if (strategy != null) {

                this.monitorScheduleStrategy = strategy;

            }

        }
        //监控对象诊断，开始
        startMonitorDiagnoser();

    }


    public boolean isMonitorScheduled(Monitor monitor) {
        return this.tasks.containsKey(monitor.getId());
    }

    //开始一个监控对象的任务
    public synchronized void scheduleMonitor(Monitor monitor) {
        long startTime = System.currentTimeMillis();
        String connectionName = monitor.getConnection().getName();

        if (log.isDebugEnabled()) {
            log.debug("Dump all the scheduled task IDs {} before scheduling monitor '{}'...",
                    Arrays.toString(this.tasks.keySet().toArray()), monitor.getName());
        }

        int maxActiveMonitors = this.configuration.getMaxActiveMonitors();
        if (maxActiveMonitors > 0 &&
                this.tasks.size() + 1 > maxActiveMonitors) {
            throw new MonitorScheduleNotAllowedException(ErrorConstants.Message.MSG_MONITOR_SCHEDULE_NOT_ALLOWED_1, new Object[]{monitor.getName(), connectionName});
        }
        if (log.isDebugEnabled()) {
            log.debug("maxActiveMonitors {}", Integer.valueOf(maxActiveMonitors));
        }

        if (this.monitorScheduleStrategy != null && !this.monitorScheduleStrategy.canSchedule(monitor)) {
            throw new MonitorScheduleNotAllowedException(ErrorConstants.Message.MSG_MONITOR_SCHEDULE_NOT_ALLOWED_2, new Object[]{monitor.getName(), connectionName});
        }

        if (isMonitorScheduled(monitor)) {
            throw new MonitorAlreadyScheduledException(ErrorConstants.Message.MSG_MONITOR_ALREADY_SCHEDULED, new Object[]{monitor.getName(), connectionName});
        }

        try {

            Time time = Time.parseSMHD(monitor.getPollingInterval());

            if (log.isDebugEnabled()) {
                log.debug("Monitor pollingInterval {}", time);

            }

            MonitorTask task = new MonitorTask(monitor, this.commandProcessor, this.statisticalDataProcessor);

            ScheduledFuture<?> scheduledMonitorHandle =
                    this.scheduledExecutorService.scheduleAtFixedRate(task, 0L, time.getValue(), time.getUnit());

            ScheduledFuture<?> existing = this.tasks.put(monitor.getId(), scheduledMonitorHandle);
            //如果存在旧值，将旧值取消掉不在执行
            if (existing != null) {
                existing.cancel(true);
                if (log.isDebugEnabled()) {
                    log.debug("Scheduled monitor task with scheduleId '{}' is already exists for '{}'!",
                            monitor.getId(), monitor.getName());

                }

            }
            //将监控对象放入诊断队列中
            this.diagnosedTasks.put(monitor.getId(), task);

            if (log.isDebugEnabled()) {
                log.debug("'{}' is scheduled with scheduleId '{}' successfully with fixed rate {}!", new Object[]{monitor
                        .getName(), monitor.getId(), time});
            }
        } catch (Exception e) {
            throw new MonitorSchedulerException(e);
        }

        if (log.isDebugEnabled()) {
            log.debug("{} ms spent for scheduling '{}'", Long.valueOf(System.currentTimeMillis() - startTime), monitor.getName());
        }

        logScheduledExecutorStats();

    }

    //取消monitor监控任务
    public void unscheduleMonitor(Monitor monitor) {
        String mqKey = "mq_unschedule";
        synchronized (mqKey.intern()) {
            long startTime = System.currentTimeMillis();
            String connectionName = monitor.getConnection().getName();
            String monitorName = monitor.getName();

            if (log.isDebugEnabled()) {
                log.debug("Dump all the scheduled task IDs: {} before unscheduleing '{}' ...",
                        Arrays.toString(this.tasks.keySet().toArray()), monitor.getName());
            }

            if (!isMonitorScheduled(monitor)) {
                throw new MonitorNotScheduledException(ErrorConstants.Message.MSG_MONITOR_NOT_SCHEDULED, new Object[]{monitorName, connectionName});
            }

            ScheduledFuture<?> scheduledMonitorHandle = this.tasks.get(monitor.getId());

            if (log.isDebugEnabled()) {

                log.debug("Found scheduled task with scheduledId '{}' for '{}'!", monitor.getId(), monitor.getName());

            }
            //任务取消掉
            boolean cancelled = scheduledMonitorHandle.cancel(true);

            if (log.isDebugEnabled()) {
                log.debug("Scheduled task cancelled: {}, isDone: {}", Boolean.valueOf(cancelled), Boolean.valueOf(scheduledMonitorHandle.isDone()));
            }
            //删掉此monitor的监控
            this.tasks.remove(monitor.getId());

            if (log.isDebugEnabled()) {

                log.debug("Task for monitor '{}' is cancelled in connection '{}', and begin cleaning diagnose task for this monitor...", monitorName, connectionName);

            }
            //在诊断任务中删除此监控对象
            IDiagnosable diagnosable = this.diagnosedTasks.remove(monitor.getId());
            if (diagnosable == null) {
                log.error(ErrorConstants.Message.binds(ErrorConstants.Message.MSG_INTERNAL_SERVER_ERROR, new Object[]{
                        ErrorConstants.Message.binds(ErrorConstants.Message.MSG_MONITOR_DIAGNOSER_CANCELLATION_FAILURE, new Object[]{monitorName, connectionName})
                }));
            }

            if (log.isDebugEnabled()) {
                log.debug("'{}' is unscheduled successfully!.", monitor.getName());
            }

            if (log.isDebugEnabled()) {
                log.debug("{} ms spent for unscheduling '{}'", Long.valueOf(System.currentTimeMillis() - startTime), monitor.getName());
            }

            logScheduledExecutorStats();
        }
    }

    //重启monitor监控任务
    public synchronized void rescheduleMonitor(Monitor monitor) {
        long startTime = System.currentTimeMillis();

        if (isMonitorScheduled(monitor)) {
            unscheduleMonitor(monitor);
            scheduleMonitor(monitor);
            if (log.isDebugEnabled()) {

                log.debug("'{}' is rescheduled.", monitor.getName());

            }

        }

        if (log.isDebugEnabled()) {

            log.debug("{} ms spent for rescheduling '{}'", Long.valueOf(System.currentTimeMillis() - startTime), monitor.getName());

        }

        logScheduledExecutorStats();

    }


    private void logScheduledExecutorStats() {
        if (log.isDebugEnabled()) {
            log.debug("ScheduledExecutorService stats: {}", this.scheduledExecutorService.stats());
        }

    }


    @PreDestroy
    private void shutdown() {
        this.scheduledExecutorService.shutdownNow();
    }


    private void startMonitorDiagnoser() {
        String diagnoseInterval = this.configuration.getMonitorDiagnoseInterval();
        long initialDelay = 60L;//默认初始化延迟时间
        long interval = 10L;//访问次数
        TimeUnit unit = TimeUnit.SECONDS;

        try {
            Time time = Time.parseSMHD(diagnoseInterval);
            interval = time.convertTo(unit);
        } catch (Exception e) {
            log.warn(String.format("Failed to parse diagnoseInterval from config file, using defaults '%d' seconds.", new Object[]{
                    Long.valueOf(interval)}), e);
        }

        this.scheduledExecutorService.scheduleWithFixedDelay(new MonitorDiagnoser(), initialDelay, interval, unit);
    }

    class MonitorDiagnoser implements Runnable {
        public void run() {

            if (MonitorScheduler.this.diagnoseStrategy == null) {
                if (log.isDebugEnabled()) {
                    log.debug("没有设置健康规则，无法判断监视任务的健康状态!");
                }
                return;
            }

            try {
                if (log.isDebugEnabled()) {
                    log.debug("计划检测监控任务...");
                }
                Iterator<IDiagnosable> iter = MonitorScheduler.this.diagnosedTasks.values().iterator();

                while (iter.hasNext()) {
                    IDiagnosable diagnosable = iter.next();
                    //评估是否要将监控的MQ对象删除掉，qmr不会被删除，其他的通过失败率和期望失败次数删除任务
                    if (!MonitorScheduler.this.diagnoseStrategy.isHealthy(diagnosable)) {
                        Monitor monitor = (Monitor) diagnosable.adapt(Monitor.class);
                        if (monitor == null) {
                            if (log.isDebugEnabled()) {
                                log.debug("队列中可监控的 '{}' 无法成功监控!", diagnosable);
                            }
                            continue;
                        }
                        try {
                            if (log.isDebugEnabled()) {
                                log.debug("删除不健康的监控 '{}' ...", monitor.getName());
                            }
                            MonitorScheduler.this.unscheduleMonitor(monitor);
                            log.info("自动停用不健康的监视任务 '{}'!", monitor.getName());
                            iter.remove();
                            if (log.isWarnEnabled()) {
                                log.warn("自动停用不健康的监视任务 '{}'!", monitor.getName());
                            }
                        } catch (Exception e) {
                            log.error(String.format("监视器 '%s' 任务取消失败!", new Object[]{monitor.getName()}), e);
                        }
                    }
                }
            } catch (Exception e) {
                log.error("无法判断的异常!", e.getMessage());
            }
        }
    }
}