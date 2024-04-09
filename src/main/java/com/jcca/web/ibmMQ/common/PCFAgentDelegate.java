package com.jcca.web.ibmMQ.common;

import com.ibm.mq.MQException;
import com.ibm.mq.MQQueueManager;
import com.ibm.mq.pcf.PCFMessage;
import com.ibm.mq.pcf.PCFMessageAgent;
import com.jcca.web.ibmMQ.command.impl.PCFMessageFactory;
import com.jcca.web.ibmMQ.domain.Connection;
import com.jcca.web.ibmMQ.util.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.concurrent.ThreadSafe;
import java.io.IOException;
import java.util.Hashtable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


@ThreadSafe
public final class PCFAgentDelegate {
    private static final Logger log = LoggerFactory.getLogger(PCFAgentDelegate.class);
    private static final int DEFAULT_WAIT_INTERVAL = 15;
    private final Lock lock = new ReentrantLock();
    private final Lock sendLock = new ReentrantLock();
    private final Connection connection;

    private final PCFMessageAgent messageAgent;
    private volatile boolean isV7;
    private MQQueueManager queueManager;

    public PCFAgentDelegate(Connection connection) throws MQException {
        Assert.notNull(connection, "Connection cannot be null!");
        this.connection = connection;
        this.queueManager = newQueueManager(connection);
        this.messageAgent = new PCFMessageAgent(this.queueManager);
        if (log.isDebugEnabled()) {
            log.debug("新建 MQGR 连接 with '{}'", connection.getName());
        }
    }

    private MQQueueManager newQueueManager(final Connection connection) throws MQException {
        return new MQQueueManager((String) null, new Hashtable<String, Object>(5) {
            {
                this.put("port", connection.getPort());
                this.put("hostname", connection.getHost());
                this.put("channel", connection.getChannelName());
                String userId = connection.getUserId();
                if (userId != null) {
                    this.put("userID", userId);
                }

                this.put("transport", "MQSeries");
            }
        });
    }

    public void connect() throws MQException {
        Lock lock = this.lock;
        lock.lock();
        try {
            try {
                this.messageAgent.disconnect();
            } catch (Exception e) {
                if (log.isDebugEnabled()) {
                    log.debug("PCFMessageAgent encounter error when disconnecting.", e);
                }
            }
            this.queueManager = newQueueManager(this.connection);
            this.messageAgent.connect(this.queueManager);
        } finally {
            lock.unlock();
        }
    }

    public boolean isConnected() {
        PCFMessage request = this.isV7 ? PCFMessageFactory.createInquireQMgrV7() : PCFMessageFactory.createInquireQMgr();
        try {
            send(request, 15);
            return true;
        } catch (Exception e) {
            if (e instanceof MQException) {
                if (((MQException) e).getReason() == 2067) {
                    this.isV7 = true;
                    try {
                        send(PCFMessageFactory.createInquireQMgrV7(), 15);
                        return true;
                    } catch (Exception e1) {
                        log.error(String.format("PCFMessageAgent cannot connect to QMGR for '%s'!", new Object[]{this.connection}), e1);
                    }
                }
            }
            return false;
        }
    }

    public PCFMessage[] send(PCFMessage request, int seconds) throws MQException, IOException {
        Lock sendLock = this.sendLock;
        sendLock.lock();
        try {
            this.messageAgent.setWaitInterval(seconds);
            return this.messageAgent.send(request);
        } catch (MQException e) {
            int rc = e.getReason();
            switch (rc) {
                case 2162:
                case 2202:
                    try {
                        //   log.error("Reason code:{}, message: {}", Integer.valueOf(rc), MQConstants.lookupReasonCode(rc));
                        TimeUnit.SECONDS.sleep(2L);
                    } catch (InterruptedException e1) {
                    }
                    break;
                case 2009:
                case 2538:
                    connect();
                    // log.error("Reason code:{}, message: {}, reconnecting later...", Integer.valueOf(rc), MQConstants.lookupReasonCode(rc));
                    break;
            }
            throw e;
        } finally {
            sendLock.unlock();
        }
    }

    public Connection getConnection() {
        return this.connection;
    }

    public String getQueueManagerName() {
        return this.messageAgent.getQManagerName();
    }

    public MQQueueManager getQueueManager() {
        return this.queueManager;
    }

    public void disconnect() throws MQException {
        Lock lock = this.lock;
        lock.lock();
        try {
            this.messageAgent.disconnect();
            this.queueManager.disconnect();
        } finally {
            lock.unlock();
        }
    }

    public String toString() {
        return getClass().getSimpleName() + " => " + this.connection.getName();
    }
}

