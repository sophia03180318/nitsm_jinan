package com.jcca.web.ibmMQ.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ibm.mq.MQException;
import com.jcca.web.ibmMQ.common.ErrorConstants;
import com.jcca.web.ibmMQ.common.MetadataException;
import com.jcca.web.ibmMQ.common.MetadataNotFoundException;
import com.jcca.web.ibmMQ.common.MonitoringException;
import com.jcca.web.ibmMQ.config.Configuration;
import com.jcca.web.ibmMQ.dao.*;
import com.jcca.web.ibmMQ.domain.Connection;
import com.jcca.web.ibmMQ.domain.Monitor;
import com.jcca.web.ibmMQ.entity.IBMConnection;
import com.jcca.web.ibmMQ.entity.IBMMonitor;
import com.jcca.web.ibmMQ.entity.IBMQMgrData;
import com.jcca.web.ibmMQ.schedule.IMonitorScheduler;
import com.jcca.web.ibmMQ.service.*;
import com.jcca.web.ibmMQ.support.BeanLocator;
import com.jcca.web.ibmMQ.support.IConnectionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author zhaozheng
 * @date 2020-07-14 15:56
 **/
@Service
public class ConnectionServiceImpl extends ServiceImpl<ConnectionMapper, IBMConnection> implements ConnectionService {
    private static final Logger log = LoggerFactory.getLogger(ConnectionServiceImpl.class);
    private final List<IConnectionListener> connectionListeners = new ArrayList<IConnectionListener>();
    @Resource(name = "monitorScheduler")
    private IMonitorScheduler monitorScheduler;
    @Resource
    private IBMMeasurementsMapper ibmMeasurementsMapper;

    @Resource
    private ConnectionMapper connectionMapper;

    @Resource
    private QMgrDataMapper qMgrDataMapper;
    @Resource
    private ChannelDataMapper channelDataMapper;
    @Resource
    private QueueDataMapper queueDataMapper;
    @Resource
    private TopicDataMapper topicDataMapper;
    @Resource
    private ListenerDataMapper listenerDataMapper;
    @Resource
    private IBMMonitorService ibmMonitorService;
    @Resource
    private QMgrDataService qMgrDataService;
    @Resource
    private PCFMessageService pcfMessageService;
    @Resource
    private MonitorService monitorService;


    private TopicSubscriptionService topicSubscriptionService;

    @Resource(name = "beanLocator")
    private BeanLocator beanLocator;

    @Resource
    private IMetadataService metadataService;
    @Resource
    private Configuration configuration;

    @Override
    public List<Connection> listConnections() {
        List<Connection> list = connectionMapper.findConnections();
        return list;
    }

    @PostConstruct
    private void init() {
        MQException.log = null;

        Collection<IConnectionListener> listeners = this.beanLocator.findBeans(IConnectionListener.class);
        if (log.isDebugEnabled()) {
            log.debug("Found connection listeners {} ", listeners);
        }
        this.connectionListeners.addAll(listeners);
        this.topicSubscriptionService = new TopicSubscriptionService(this.metadataService, this.configuration);
    }

    @Override
    public Connection createConnection(Connection connection) {

        try {
            this.pcfMessageService.testConnection(connection);
        } catch (Exception var7) {
            if (var7 instanceof MonitoringException) {
                throw (MonitoringException) var7;
            }
            throw new MetadataException(50000, ErrorConstants.Message.MSG_INTERNAL_SERVER_ERROR, new Object[]{var7.getMessage()}, var7);
        }
        try {
            this.pcfMessageService.connect(connection);
        } catch (Exception var6) {
            if (var6 instanceof MonitoringException) {
                throw (MonitoringException) var6;
            }

            throw new MetadataException(50000, ErrorConstants.Message.MSG_INTERNAL_SERVER_ERROR, new Object[]{var6.getMessage()}, var6);
        }
        try {
            IBMConnection ibmConnection = Connection.getIBMConnection(connection);
            this.save(ibmConnection);
            connection.setId(ibmConnection.getId());
        } catch (Exception var5) {
            throw new MetadataException(50000, ErrorConstants.Message.MSG_INTERNAL_SERVER_ERROR, new Object[]{var5.getMessage()}, var5);
        }

        Monitor defaultMonitor = this.monitorService.createMonitor(connection);
        this.topicSubscriptionService.createDestinationQueue(connection);
        String canActivate = System.getProperty("monitor.qmgr.default.activateOnCreation", Boolean.TRUE.toString());
        if (Boolean.valueOf(canActivate)) {
            this.monitorService.activateMonitor(defaultMonitor);
        }

        if (log.isInfoEnabled()) {
            log.info("'{}' is created", connection.getName());
        }

        return Connection.getConnection(this.findByName(connection.getName()).get(0));

    }

    @Override
    public List<IBMConnection> findByName(String connectionName) {
        return connectionMapper.findByName(connectionName);
    }

    @Override
    public List<IBMConnection> findByHostAndPort(String host, Integer port) {
        return connectionMapper.findByHostAndPort(host, port);
    }

    @Override
    public Connection findConnection(String connectionName) {
        return this.findConnectionFailIfNotExists(connectionName);
    }

    @Override
    public void removeConnection(String connectionId, String connectionName) {
        Connection connection = this.findConnectionFailIfNotExists(connectionName);
        if (log.isDebugEnabled()) {
            log.debug("Preparing removal preconditions for connection '{}'", connectionName);
        }

/*        List<IBMMonitor> monitors = this.monitorService.listMonitors(connectionName, null, (Monitor.Scope) null);
        for (IBMMonitor ibmMonitor : monitors) {
            Monitor monitor = Monitor.getMonitor(ibmMonitor);
            if (monitor.isDefault()) {
                if (monitor.isActive()) {
                    try {
                        monitorService.removeMonitor(monitor);
                    } catch (Exception var10) {
                        log.error(var10.getLocalizedMessage(), var10);
                    }
                    this.monitorService.removeById(monitor.getId());
                    ibmMeasurementsMapper.deleteMeasurements(monitor.getId());
                    removeRelateInfo(monitor.getId(), monitor.getCategory().getValue());
                }
            } else {
                this.monitorService.removeById(monitor.getId());
                ibmMeasurementsMapper.deleteMeasurements(monitor.getId());
                removeRelateInfo(monitor.getId(), monitor.getCategory().getValue());
            }

        }*/

        this.topicSubscriptionService.deleteDestinationQueue(connection);
        for (IConnectionListener listener : connectionListeners) {
            try {
                listener.beforeRemovingConnection(connection);
            } catch (Exception var9) {
                log.error("Got error in connectionListener.beforeRemovingConnection!", var9);
            }
        }

        Connection clonedConnection = connection.clone();

        try {
            String id = connection.getId();

            //清除IBMMQ_MONITOR表数据
            QueryWrapper<IBMMonitor> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("CATEGORY", "QueueManager");
            queryWrapper.eq("CONNECTIONID", id);
            List<IBMMonitor> list = ibmMonitorService.list(queryWrapper);
            if (!list.isEmpty()) {
                //关闭采集任务
                IBMMonitor monitor = list.get(0);
                IBMConnection ibmConnection = this.getById(id);
                monitor.setHost(ibmConnection.getConnectHost());
                monitor.setPort(ibmConnection.getConnectPort());
                monitor.setConnectionName(ibmConnection.getConnectName());
                monitor.setConnectionId(ibmConnection.getId());
                monitor.setConnectionDescription(ibmConnection.getDescription());
                monitor.setChannelName(ibmConnection.getChannelName());
                monitor.setUserId(ibmConnection.getUserId());
                Monitor monitor2 = Monitor.getMonitor(monitor);
                monitorService.removeTask(monitor2);


                ibmMonitorService.remove(queryWrapper);
                List<String> idList = list.stream().map(IBMMonitor::getId).collect(Collectors.toList());
                //清除IBMMQ_QMGR_DATA
                QueryWrapper<IBMQMgrData> query = new QueryWrapper<>();
                query.in("MONITOR_ID", idList);
                qMgrDataService.remove(query);

            }
            this.removeById(id);

            if (log.isInfoEnabled()) {
                log.info("'{}' is removed", connection.getName());
            }
        } catch (Exception var11) {
            throw new MetadataException(50000, ErrorConstants.Message.MSG_INTERNAL_SERVER_ERROR, new Object[]{var11.getMessage()}, var11);
        }

        Iterator var14 = this.connectionListeners.iterator();

        while (var14.hasNext()) {
            IConnectionListener listener = (IConnectionListener) var14.next();

            try {
                listener.afterConnectionRemoved(clonedConnection);
            } catch (Exception var8) {
                log.error("Got error in connectionListener.afterConnectionRemoved!", var8);
            }
        }


    }

    private Connection findConnectionFailIfNotExists(String connectionName) {
        IBMConnection ibmConnection = this.findByName(connectionName).get(0);
        if (ibmConnection == null) {
            throw new MetadataNotFoundException(40402, ErrorConstants.Message.MSG_CONNECTION_NOT_FOUND, new Object[]{connectionName});
        } else {
            return Connection.getConnection(ibmConnection);
        }
    }


    private void removeRelateInfo(String monitorId, String category) {
        switch (category) {
            case "QueueManager":
                qMgrDataMapper.removeStatistics(monitorId);
                break;
            case "Channel":
                channelDataMapper.removeStatistics(monitorId);
                break;
            case "Queue":
                queueDataMapper.removeStatistics(monitorId);
                break;
            case "Listener":
                listenerDataMapper.removeStatistics(monitorId);
                break;
            case "Topic":
                topicDataMapper.removeStatistics(monitorId);
                break;
        }
    }

}