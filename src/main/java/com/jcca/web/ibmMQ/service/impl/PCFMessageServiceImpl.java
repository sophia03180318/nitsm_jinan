package com.jcca.web.ibmMQ.service.impl;


import com.ibm.mq.MQException;
import com.ibm.mq.constants.MQConstants;
import com.ibm.mq.pcf.PCFMessage;
import com.jcca.web.ibmMQ.common.*;
import com.jcca.web.ibmMQ.config.Configuration;
import com.jcca.web.ibmMQ.domain.Connection;
import com.jcca.web.ibmMQ.entity.IBMConnection;
import com.jcca.web.ibmMQ.service.ConnectionService;
import com.jcca.web.ibmMQ.service.PCFMessageService;
import com.jcca.web.ibmMQ.util.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


@Component("messageService")
public class PCFMessageServiceImpl implements PCFMessageService {
    private static final Logger log = LoggerFactory.getLogger(PCFMessageServiceImpl.class);
    private final Map<String, PCFAgentDelegate> messageAgents = new ConcurrentHashMap<String, PCFAgentDelegate>();
    private final Lock lock = new ReentrantLock();

    @Resource(name = "configuration")
    private Configuration configuration;

    @Resource
    private ConnectionService connectionService;

    @Override
    public void connect(Connection conn) throws Exception {
        Assert.notNull(conn, "Connection cannot be null!");
        Connection connection = conn.clone();
        Lock lock = this.lock;
        lock.lock();
        try {
            PCFAgentDelegate agent = getMessageAgent0(connection);
            if (log.isDebugEnabled()) {
                log.debug("Find PCF message agent '{}' for connection '{}' with id {}.", new Object[]{agent, connection.getName(), Integer.valueOf(connection.getId())});
            }
            if (agent != null) {
                if (agent.isConnected()) {
                    if (log.isDebugEnabled()) {
                        log.debug("PCF message agent '{}' for connection '{}' is already connected.", agent, connection
                                .getName());
                    }
                    return;
                }
                try {
                    if (log.isDebugEnabled()) {
                        log.debug("Try to connect PCF message agent '{}' for connection '{}' is already connected.", agent, connection
                                .getName());
                    }
                    agent.connect();
                    if (log.isDebugEnabled()) {
                        log.debug("PCF message agent '{}' for connection '{}' is connected.", agent, connection
                                .getName());
                    }
                    return;
                } catch (Exception e) {
                    log.warn(String.format("Error when reconnect PCF message agent '%s'!", new Object[]{agent}), e);
                }
            }
            if (log.isDebugEnabled()) {
                log.debug("No PCF message agent is available for connection '{}', creating a new agent...", connection
                        .getName());
            }
            try {
                createPCFMessageAgent(connection);
            } catch (Exception e) {
                this.messageAgents.remove(connection.getName());
                checkConnectionException(connection, e);
                throw e;
            }
        } finally {
            lock.unlock();
        }
        if (log.isDebugEnabled()) {
           // log.debug("Connect operation finished for connect '{}'", connection.getName());
        }
    }

    @Override
    public boolean isConnected(Connection connection) {
        Assert.notNull(connection, "Connection cannot be null!");
        try {
            PCFAgentDelegate agent = getMessageAgent0(connection.clone());
            if (log.isDebugEnabled()) {
                log.debug("PCFAgent '{}'", agent);
                if (agent != null) {
                    log.debug("PCFAgent isConnected '{}'", Boolean.valueOf(agent.isConnected()));
                }
            }
            return (agent != null && agent.isConnected());
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug(String.format("Error when checking '%s'!", new Object[]{connection.getName()}), e);
            }
            return false;
        }
    }

    @Override
    public void testConnection(Connection connection) throws Exception {
        Assert.notNull(connection, "Connection cannot be null!");
        PCFAgentDelegate agent = null;

        try {
            agent = new PCFAgentDelegate(connection);
        } catch (Exception var11) {
            checkConnectionException(connection, var11);
            throw var11;
        } finally {
            try {
                if (agent != null) {
                    agent.disconnect();
                }
            } catch (Exception var10) {
                log.warn("Error when disconnect PCF message agent!", var10);
            }

        }

    }

    @Override
    public boolean existsConnection(String connectionName) {
        List<IBMConnection> list = connectionService.findByName(connectionName);
        if (list != null && list.size() > 0) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public PCFMessage[] sendRequest(Connection connection, PCFMessage request, int timeout, boolean autoConnect) throws Exception {
        Assert.notNull(connection, "Connection cannot be null!");
        Assert.notNull(request, "PCFMessage request cannot be null!");
        PCFAgentDelegate messageAgent = getMessageAgent(connection.clone(), autoConnect);
        if (timeout <= 0) {
            timeout = this.configuration.getMonitorConnectionTimeout();
        }
        if (log.isDebugEnabled()) {
            log.debug("Sending request to QMGR '{}' with waitInterval {} seconds...", messageAgent
                    .getQueueManagerName(), Integer.valueOf(timeout));
        }
        return messageAgent.send(request, timeout);
    }

    @Override
    public String getQueueManagerName(Connection connection, boolean autoConnect) throws Exception {
        Assert.notNull(connection, "Connection cannot be null!");
        PCFAgentDelegate messageAgent = getMessageAgent(connection.clone(), autoConnect);
        return messageAgent.getQueueManagerName();
    }


    private PCFAgentDelegate getMessageAgent0(Connection connection) {
        PCFAgentDelegate agent = this.messageAgents.get(connection.getName());
        return agent;
    }

    private PCFAgentDelegate createPCFMessageAgent(Connection connection) throws MQException {
        PCFAgentDelegate agent = new PCFAgentDelegate(connection);
        if (log.isDebugEnabled()) {
            log.debug("Creating PCF message agent '{}' for connection '{}' with id '{}'.", new Object[]{agent, connection
                    .getName(), Integer.valueOf(connection.getId())});
        }
        PCFAgentDelegate oldAgent = this.messageAgents.put(connection.getName(), agent);
        if (oldAgent != null) {
            try {
                if (log.isDebugEnabled()) {
                    log.debug("Disconnecting old agent '{}' disconnected.", oldAgent);
                }
                oldAgent.disconnect();
                if (log.isDebugEnabled()) {
                    log.debug("Old agent '{}' disconnected.", oldAgent);
                }
            } catch (Exception e) {
                log.warn("Error when disconnect PCF message agent!", e);
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("Created PCF message agent for '{}'.", connection.getName());
        }
        return agent;
    }

    static void checkConnectionException(Connection connection, Exception e) {
        if (log.isDebugEnabled()) {
            log.debug("Checking exception for connection '{}'", connection.getName());
        }
        if (e instanceof NotConnectedException) {
            throw (NotConnectedException) e;
        }
        if (e instanceof MQException) {
            int rc = ((MQException) e).getReason();
            String reason = MQConstants.lookupReasonCode(rc);
            switch (rc) {
                case 2538:
                    throw new InvalidMetadataException(40009, ErrorConstants.Message.MSG_CONNECTION_UNREACHABLE, new Object[]{connection,
                            Integer.valueOf(rc), reason}, e);
                case 2035:
                    throw new IllegalMetadataStateException(40303, ErrorConstants.Message.MSG_CONNECTION_NOT_AUTHORIZED, new Object[]{connection,
                            Integer.valueOf(rc), reason}, e);
                case 2540:
                    throw new InvalidMetadataException(40010, ErrorConstants.Message.MSG_CONNECTION_UNKNOWN_CHANNEL_NAME, new Object[]{connection
                            .getChannelName(), Integer.valueOf(rc), reason}, e);
                case 2537:
                    throw new InvalidMetadataException(40011, ErrorConstants.Message.MSG_CONNECTION_CHANNEL_NOT_AVAILABLE, new Object[]{connection
                            .getChannelName(), Integer.valueOf(rc), reason}, e);
            }
            if (log.isDebugEnabled()) {
                log.debug("Got MQException with rc: '{}({})'", Integer.valueOf(rc), reason);
            }
        }
    }


    private PCFAgentDelegate getMessageAgent(Connection connection, boolean autoConnect) throws Exception {
        PCFAgentDelegate agent = getMessageAgent0(connection);
        if (log.isDebugEnabled()) {
           // log.debug("Find PCF message agent '{}' for connection '{}'", agent, connection.getName());
        }
        if (agent == null) {
            if (log.isDebugEnabled()) {
               // log.debug("No PCF message agent is found for '{}'", connection.getName());
            }
            if (!autoConnect) {
                throw new NotConnectedException(40902, ErrorConstants.Message.MSG_CONNECTION_NOT_CONNECTED, new Object[]{connection});
            }
            Lock lock = this.lock;
            lock.lock();
            try {
                agent = createPCFMessageAgent(connection);
            } catch (Exception e) {
                this.messageAgents.remove(connection.getName());
                checkConnectionException(connection, e);
                throw e;
            } finally {
                lock.unlock();
            }
        }
        try {
            if (!agent.isConnected()) {
                if (log.isDebugEnabled()) {
                   // log.debug("PCF message agent for '{}' is not connected, try to connect...", connection.getName());
                }
                agent.connect();
                if (log.isDebugEnabled()) {
                  //  log.debug("PCF message agent for '{}' is connected", connection.getName());
                }
            }
        } catch (Exception e) {
          //  log.warn(String.format("Error when reconnect PCF message agent '%s'!", new Object[]{agent}), e);
        }
        return agent;
    }


}