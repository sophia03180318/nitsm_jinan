package com.jcca.web.ibmMQ.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcca.web.ibmMQ.common.*;
import com.jcca.web.ibmMQ.config.Configuration;
import com.jcca.web.ibmMQ.dao.IBMMeasurementsMapper;
import com.jcca.web.ibmMQ.dao.MonitorMapper;
import com.jcca.web.ibmMQ.domain.Connection;
import com.jcca.web.ibmMQ.domain.Monitor;
import com.jcca.web.ibmMQ.entity.IBMMeasurements;
import com.jcca.web.ibmMQ.entity.IBMMonitor;
import com.jcca.web.ibmMQ.health.IHealthEvaluator;
import com.jcca.web.ibmMQ.schedule.*;
import com.jcca.web.ibmMQ.service.IBMMeasurementsService;
import com.jcca.web.ibmMQ.service.IMetadataService;
import com.jcca.web.ibmMQ.service.MonitorService;
import com.jcca.web.ibmMQ.service.PCFMessageService;
import com.jcca.web.ibmMQ.support.BeanLocator;
import com.jcca.web.ibmMQ.support.IMonitorListener;
import com.jcca.web.ibmMQ.util.ArgumentChecker;
import com.jcca.web.ibmMQ.util.Assert;
import com.jcca.web.ibmMQ.util.Metadata;
import com.jcca.web.ibmMQ.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.*;


/**
 * @author zhaozheng
 * @date 2020-07-14 15:28
 **/
@Service
public class MonitorServiceImpl extends ServiceImpl<MonitorMapper, IBMMonitor> implements MonitorService {
    private static final Logger log = LoggerFactory.getLogger(MonitorServiceImpl.class);
    private final List<IMonitorListener> monitorListeners = new ArrayList();
    @Resource
    private MonitorMapper monitorMapper;
    @Resource
    private IBMMeasurementsMapper ibmMeasurementsMapper;
    @Resource(name = "metadataService")
    private IMetadataService metadataService;

    @Resource
    private IBMMeasurementsService ibmMeasurementsService;

    @Resource(name = "configuration")
    private Configuration configuration;
    @Resource(name = "healthEvaluator")
    private IHealthEvaluator healthEvaluator;

    @Resource(name = "monitorScheduler")
    private IMonitorScheduler monitorScheduler;
    @Resource
    private PCFMessageService pcfMessageService;

    @Resource(name = "beanLocator")
    private BeanLocator beanLocator;

    @Autowired
    DataSourceTransactionManager dataSourceTransactionManager;
    @Autowired
    TransactionDefinition transactionDefinition;

    @PostConstruct
    private void init() {
        Collection<IMonitorListener> listeners = this.beanLocator.findBeans(IMonitorListener.class);
        if (log.isDebugEnabled()) {
            log.debug("Found monitor listeners {} ", listeners);
        }

        this.monitorListeners.addAll(listeners);
    }


    @Override
    public List<IBMMonitor> listMonitors(String connectName, String paramState, Monitor.Scope paramScope) {
        List<IBMMonitor> list = monitorMapper.listMonitors(connectName, paramState, paramScope == null ? null : paramScope.getValue());
        return list;
    }

    @Override
    public IBMMonitor updateMonitorState(String connectionName, String monitorName, Monitor.State newState) {
        IBMMonitor monitor = findMonitor(connectionName, monitorName);
        if (monitor.getState().equals(newState.getValue())) {
            return monitor;
        }
        try {
            monitor.setState(newState.getValue());
            this.saveOrUpdate(monitor);
            return monitor;
        } catch (Exception e) {
            throw new MetadataException(50000, ErrorConstants.Message.MSG_INTERNAL_SERVER_ERROR, new Object[]{e.getMessage()}, e);
        }
    }

    @Override
    public IBMMonitor findMonitor(String connectionName) {
        return this.findMonitorFailIfNotExists(connectionName, null);
    }

    public IBMMonitor findMonitor(String connectionName, String monitorName) {
        return findMonitorFailIfNotExists(connectionName, monitorName);
    }

    @Override
    public Monitor getMonitor(String connectionName, String monitorName) {
        List<IBMMonitor> list = monitorMapper.listMonitors(connectionName, monitorName, null);
        if (list != null && list.size() > 0) {
            return Monitor.getMonitor(list.get(0));
        } else {
            return null;
        }

    }

    @Override
    public void activateMonitor(String paramString) {
        activateMonitor(paramString, "qmgr");
    }

    @Transactional
    public void activateMonitor(String connectionName, String monitorName) {
        Monitor monitor = this.getMonitor(connectionName, monitorName);
        activateMonitor(monitor);
    }

    @Override
    public Monitor createMonitor(Connection connection) {
        String qmgrName = this.metadataService.queryQMgrName(connection);
        List<String> measurements = this.metadataService.queryMeasurements(Monitor.Category.QueueManager.name(), (String) null);
        String measurement = (String) measurements.get(0);
        Monitor monitor = new Monitor(connection);
        monitor.setName(Monitor.MonitorNameType.QMGR.getValue());
        monitor.setObjectName(qmgrName);
        monitor.setCategory(Monitor.Category.QueueManager);
        monitor.setObjectType(Monitor.Category.QueueManager.name());
        monitor.addMeasurement(measurement);
        monitor.setViewType(Monitor.ViewType.AreaChart);
        monitor.setHealthRule("Error(queueManagerStatus=Unavailable)");
        monitor.setScope(Monitor.Scope.SYSTEM);
        monitor.setState(Monitor.State.Inactive);
        monitor.setPollingInterval("5m");
        monitor.setDataExpirationTime(this.configuration.getQmgrMonitorDataExpirationTime());
        monitor.setDescription("The default monitor for QMGR created by system!");

        monitor = this.createMonitor(monitor, false);
        if (log.isDebugEnabled()) {
            log.debug("System monitor '{}' for QMGR is created in '{}'", monitor.getName(), connection.getName());
        }

        return monitor;
    }

    @Override
    public Monitor createMonitor(Monitor monitor) {
        return this.createMonitor(monitor, true);
    }

    private Monitor createMonitor(Monitor monitor, boolean requireValidation) {
        if (requireValidation) {
            try {
                this.valiadate(monitor);
            } catch (Exception var6) {
                throw new InvalidMetadataException(40003, ErrorConstants.Message.MSG_INVALID_MONITOR, new Object[]{monitor.getName(), var6});
            }
            if (log.isDebugEnabled()) {
                log.debug("Checking monitor name existence...");
            }

        }
        TransactionStatus transactionStatus = dataSourceTransactionManager.getTransaction(transactionDefinition);
        try {
            if (log.isDebugEnabled()) {
                log.debug("Saving... '{}'", monitor);
            }
            //手动开启事务

            IBMMonitor ibmMonitor = Monitor.getIBMMonitor(monitor);
            this.save(ibmMonitor);
            monitor.setId(ibmMonitor.getId());
            QueryWrapper<IBMMeasurements> wrapper = new QueryWrapper<>();
            wrapper.eq("MONITOR_ID", monitor.getId());
            wrapper.eq("ELEMENT", monitor.getMeasurements().get(0));
            int count = ibmMeasurementsService.count(wrapper);
            if (count <= 0) {
                IBMMeasurements ibmMeasurements = new IBMMeasurements();
                ibmMeasurements.setElement(monitor.getMeasurements().get(0));
                ibmMeasurements.setMonitorId(monitor.getId());
                ibmMeasurementsService.save(ibmMeasurements);
            }


            /* if (monitor.isTopicCategory()) {
                this.createDurableSubscription(monitor);
            }*/

            if (monitor.getState() == Monitor.State.Active) {
                this.activateMonitor(monitor);
            }

            if (log.isInfoEnabled()) {
                log.info("'{}' is created.", monitor.getName());
            }
            // 手动提交事务
            dataSourceTransactionManager.commit(transactionStatus);//提交
        } catch (Exception var5) {
            var5.printStackTrace();
            // 手动回滚事务
            dataSourceTransactionManager.rollback(transactionStatus);//最好是放在catch 里面,防止程序异常而事务一直卡在哪里未提交
            throw new MetadataException(50000, ErrorConstants.Message.MSG_INTERNAL_SERVER_ERROR, new Object[]{var5.getMessage()}, var5);
        }

        return monitor;
    }

    private IBMMonitor findMonitorFailIfNotExists(String connectionName, String monitorName) {
        List<IBMMonitor> monitors = monitorMapper.findMonitorByNameConnectionName(connectionName, monitorName);
        if (monitors.size() <= 0) {
            throw new MetadataNotFoundException(40403, ErrorConstants.Message.MSG_MONITOR_NOT_FOUND, new Object[]{monitorName, connectionName});
        }
        IBMMonitor iBMmonitor = monitors.get(0);
        return iBMmonitor;
    }

    private void valiadate(Monitor monitor) {
        this.valiadate(monitor, false);
    }

    private void valiadate(Monitor monitor, boolean reserved) {
        Assert.notNull(monitor.getConnection(), "A monitor should associate to a connection!");
        ArgumentChecker.notNullOrEmpty(monitor.getName(), ErrorConstants.Message.MSG_INVALID_MONITOR_NAME, new Object[0]);
        if (!reserved) {
            ArgumentChecker.satisfy(!monitor.isDefault(), ErrorConstants.Message.MSG_INVALID_MONITOR_OBJECTNAME_3, new Object[]{monitor.getName()});
        }

        //  ArgumentChecker.validateMetadataName(monitor.getName());
        ArgumentChecker.notNullOrEmpty(monitor.getObjectName(), ErrorConstants.Message.MSG_INVALID_MONITOR_OBJECTNAME_1, new Object[0]);
        ArgumentChecker.notNull(monitor.getCategory(), ErrorConstants.Message.MSG_INVALID_MONITOR_CATEGORY_1, new Object[]{Arrays.toString(Monitor.Category.values())});
        String rule;
        String dataExpirationTime;
        if (!monitor.isMQTT()) {
            rule = monitor.getObjectType();
            dataExpirationTime = this.metadataService.queryObjectType(monitor.getConnection(), monitor.getCategory().name(), monitor.getObjectName());
            monitor.setObjectType(dataExpirationTime);
            if (log.isDebugEnabled()) {
                log.debug("Got object type '{}' for category '{}' and object name '{}' from QMGR", new Object[]{dataExpirationTime, monitor.getCategory(), monitor.getObjectName()});
            }

            if (!Strings.isNullOrEmpty(rule) && !rule.equals(dataExpirationTime)) {
                log.warn("The assigned object type '{}' is not valid, just ignored! The real object type got from QMGR is '{}'", monitor.getObjectType(), dataExpirationTime);
            }
        }
        if (!monitor.getObjectType().equals("Remote")) {
            ArgumentChecker.notNullOrEmpty(monitor.getMeasurements(), ErrorConstants.Message.MSG_INVALID_MONITOR_MEASUREMENT_1, new Object[0]);

        }
        if (!monitor.isMQTT()) {
            List<String> validMeasurements = Metadata.measurementsOf(monitor.getCategory(), monitor.getObjectType().trim());
            if (!validMeasurements.containsAll(monitor.getMeasurements())) {
                List<String> measurements = monitor.getMeasurements();
                measurements.removeAll(validMeasurements);
                ArgumentChecker.fail(ErrorConstants.Message.MSG_INVALID_MONITOR_MEASUREMENT_2, new Object[]{Arrays.toString(measurements.toArray()), Arrays.toString(validMeasurements.toArray())});
            }
        }

        rule = monitor.getHealthRule();
        if (!Strings.isNullOrEmpty(rule)) {
            this.healthEvaluator.validate(rule);
        } else {
            log.warn("Health rule is not specified for {} '{}'", monitor.getCategory(), monitor.getObjectName());
        }

        dataExpirationTime = monitor.getDataExpirationTime();
        if (Strings.isNullOrEmpty(dataExpirationTime)) {
            dataExpirationTime = this.configuration.getMonitorDataExpirationTime();
            monitor.setDataExpirationTime(dataExpirationTime);
        }

        String pollingInterval = monitor.getPollingInterval();
        if (Strings.isNullOrEmpty(pollingInterval)) {
            pollingInterval = this.configuration.getMonitorPollingInterval();
            monitor.setPollingInterval(pollingInterval);
        }


        Time pollingIntervalTime = Time.parseSMHD(pollingInterval, this.formattedMessage("pollingInterval", pollingInterval));
        Time expirationTime = Time.parseMHD(dataExpirationTime, this.formattedMessage("dataExpirationTime", dataExpirationTime));
        Time pollingIntervalTimeMin = Time.parseSMHD(this.configuration.getMonitorPollingIntervalMin());
        Time expirationTimeMax = Time.parseMHD(this.configuration.getMonitorDataExpirationTimeMax());
        ArgumentChecker.satisfy(pollingIntervalTime.is(Time.Operation.GE, pollingIntervalTimeMin), ErrorConstants.Message.MSG_INVALID_MONITOR_TIMEVALUE_1, new Object[]{pollingIntervalTime, pollingIntervalTimeMin});
        ArgumentChecker.satisfy(expirationTime.is(Time.Operation.LE, expirationTimeMax), ErrorConstants.Message.MSG_INVALID_MONITOR_TIMEVALUE_5, new Object[]{dataExpirationTime, expirationTimeMax});
        if (monitor.getViewType() == null) {
            monitor.setViewType(Monitor.ViewType.LineChart);
            if (log.isDebugEnabled()) {
                log.debug("ViewType is not specified, apply the default type '{}'", monitor.getViewType());
            }
        }

        if (monitor.getState() == null) {
            monitor.setState(Monitor.State.Inactive);
        }

        if (monitor.getScope() == null) {
            monitor.setScope(Monitor.Scope.NORMAL);
        }

    }

    private String formattedMessage(String property, String value) {
        return String.format("%s = %s", property, value);
    }

    public boolean existsMonitor(String connectionName, String monitorName) {
        List<IBMMonitor> list = monitorMapper.findMonitorByNameConnectionName(connectionName, monitorName);
        if (list != null && list.size() <= 0) {
            return false;
        } else {
            return true;
        }
    }

    private void notifyMonitorRemoval(Monitor monitor) {
        Iterator var2 = this.monitorListeners.iterator();

        while (var2.hasNext()) {
            IMonitorListener listener = (IMonitorListener) var2.next();

            try {
                listener.afterMonitorRemoved(monitor);
            } catch (Exception var5) {
                log.error("Got error in monitorListener.afterMonitorRemoved!", var5);
            }
        }

    }

    @Override
    public void removeMonitor(Monitor monitor) {
//        if (monitor.isDefault()) {
//            throw new IllegalMetadataStateException(40308, ErrorConstants.Message.MSG_DEFAULT_MONITOR_DELETION_NOT_ALLOWED, new Object[]{monitor.getName()});
//        } else {

        if (monitor.isActive()) {
            this.deactivateMonitor(monitor);
        }

        Monitor clonedMonitor = monitor.clone();

        try {
            this.removeById(monitor.getId());
            ibmMeasurementsMapper.deleteMeasurements(monitor.getId());
        } catch (Exception var7) {
            throw new MetadataException(50000, ErrorConstants.Message.MSG_INTERNAL_SERVER_ERROR, new Object[]{var7.getMessage()}, var7);
        }


        this.notifyMonitorRemoval(clonedMonitor);
        if (log.isInfoEnabled()) {
            log.info("'{}' is removed.", clonedMonitor.getName());
        }


    }

    private void removeDurableSubscription(Monitor monitor) {
        String subscriptionName = this.metadataService.querySubscriptionName(monitor);
        if (log.isDebugEnabled()) {
            log.debug("Removing subscription with name '{}' for monitor '{}'", subscriptionName, monitor.getName());
        }

        this.metadataService.deleteSubscription(monitor.getConnection(), subscriptionName);
    }

    private void createDurableSubscription(Monitor monitor) {
        Connection connection = monitor.getConnection();
        String subscriptionName = this.metadataService.querySubscriptionName(monitor);
        String topicString = null;
        if (monitor.isMQTT()) {
            topicString = monitor.getObjectName();
        } else {
            topicString = this.metadataService.queryTopicString(connection, monitor.getObjectName());
        }

        String destinationQueue = this.metadataService.querySubscriptionDestination(connection);
        if (log.isDebugEnabled()) {
            log.debug("Create durable subscription: '{}' with destinationQueue: '{}', topicString: '{}' for monitor '{}'.", new Object[]{subscriptionName, destinationQueue, topicString, monitor.getName()});
        }
        try {
            this.metadataService.createSubscription(connection, subscriptionName, topicString, destinationQueue);
        } catch (Exception e) {
            log.error("Create durable subscription: '{}' with destinationQueue: '{}', topicString: '{}' for monitor '{}',error: '{}'", new Object[]{subscriptionName, destinationQueue, topicString, monitor.getName(), e.getMessage()});
            throw e;
        }

    }

    @Transactional
    public void activateMonitor(Monitor monitor) {
        this.ensureConnected(monitor);

        try {
            if (!this.monitorScheduler.isMonitorScheduled(monitor)) {
                this.monitorScheduler.scheduleMonitor(monitor);
            }
        } catch (MonitorSchedulerException var4) {
            this.handleSchedulerException(var4);
        }

/*
        try {
            monitor.setState(Monitor.State.Active);
            this.saveOrUpdate(Monitor.getIBMMonitor(monitor));
        } catch (Exception var3) {
            this.monitorScheduler.unscheduleMonitor(monitor);
            throw new MetadataException(50000, ErrorConstants.Message.MSG_INTERNAL_SERVER_ERROR, new Object[]{var3.getMessage()}, var3);
        }
*/

        if (log.isInfoEnabled()) {
            log.info("'{}' activation succeed.", monitor.getName());
        }

    }

    public void activateMonitor2(Monitor monitor) {
        this.ensureConnected(monitor);

        try {
            if (!this.monitorScheduler.isMonitorScheduled(monitor)) {
                this.monitorScheduler.scheduleMonitor(monitor);
                log.info("已成功新建队列" + monitor.getName() + "监控任务:规则为" + monitor.getHealthRule());
            }
        } catch (MonitorSchedulerException var4) {
            log.error("新建队列" + monitor.getName() + "监控任务失败:规则为" + monitor.getHealthRule());
            this.handleSchedulerException(var4);
        }


    }

    @Override
    public void validateWarn(String rule) {
        this.healthEvaluator.validate(rule);
    }

    @Override
    public void cleanWarn(String id) {
        monitorMapper.cleanWarn(id);
    }

    @Override
    public void updateWarn(String monitorId, String rule) {
        monitorMapper.updateWarn(monitorId, rule);
    }

    @Override
    public void restartTask(Monitor monitor) {
        this.deactivateMonitor2(monitor);
        this.activateMonitor2(monitor);
    }

    @Override
    public void removeTask(Monitor monitor) {
        this.deactivateMonitor2(monitor);
    }


    @Override
    public void removeEvent(String objectName) {
        monitorMapper.deleteAlarmInfo(objectName);
        monitorMapper.deleteAlarmEventREL(objectName);
        monitorMapper.deleteAlarmEvent(objectName);
    }


    private void ensureConnected(Monitor monitor) {
        Connection connection = monitor.getConnection();
        if (!this.pcfMessageService.isConnected(connection)) {
            throw new NotConnectedException(40902, ErrorConstants.Message.MSG_CONNECTION_NOT_CONNECTED, new Object[]{connection});
        }
    }

    private void handleSchedulerException(MonitorSchedulerException e) {
        if (e instanceof MonitorAlreadyScheduledException) {
            throw new MonitorAlreadyActivatedException(40905, e);
        } else if (e instanceof MonitorNotScheduledException) {
            throw new MonitorAlreadyDeactivatedException(40906, e);
        } else if (e instanceof MonitorScheduleNotAllowedException) {
            throw new MonitorActivationNotAllowedException(40309, e);
        } else {
            throw new MetadataException(50000, ErrorConstants.Message.MSG_INTERNAL_SERVER_ERROR, new Object[]{e.getMessage()}, e);
        }
    }

    @Transactional
    public void deactivateMonitor(Monitor monitor) {
        //不需要再判断连接
        // this.ensureConnected(monitor);

        try {
            if (this.monitorScheduler.isMonitorScheduled(monitor)) {
                this.monitorScheduler.unscheduleMonitor(monitor);
            }
        } catch (MonitorSchedulerException var4) {
            this.handleSchedulerException(var4);
        }

/*
        try {
            monitor.setState(Monitor.State.Inactive);
            this.save(Monitor.getIBMMonitor(monitor));
        } catch (Exception var3) {
            var3.printStackTrace();
            this.monitorScheduler.scheduleMonitor(monitor);
            throw new MetadataException(50000, ErrorConstants.Message.MSG_INTERNAL_SERVER_ERROR, new Object[]{var3.getMessage()}, var3);
        }
*/

        this.notifyMonitorDeactivation(monitor);
        if (log.isInfoEnabled()) {
            log.info("'{}' deactivation succeed!", monitor.getName());
        }

    }

    public void deactivateMonitor2(Monitor monitor) {

        try {
            if (this.monitorScheduler.isMonitorScheduled(monitor)) {
                this.monitorScheduler.unscheduleMonitor(monitor);
                log.info("已成功停止队列" + monitor.getName() + "监控任务:规则为:" + monitor.getHealthRule());
            }
        } catch (MonitorSchedulerException var4) {
            log.error("停止队列" + monitor.getName() + "监控任务失败:规则为:" + monitor.getHealthRule());
            this.handleSchedulerException(var4);
        }

        this.notifyMonitorDeactivation(monitor);
    }

    private void notifyMonitorDeactivation(Monitor monitor) {
        Iterator var2 = this.monitorListeners.iterator();

        while (var2.hasNext()) {
            IMonitorListener listener = (IMonitorListener) var2.next();

            try {
                listener.afterMonitorDeactivated(monitor);
            } catch (Exception var5) {
                log.error("Got error in monitorListener.afterMonitorDeactivated!", var5);
            }
        }

    }


}