package com.jcca.web.ibmMQ.schedule.impl;


import cn.hutool.core.lang.Assert;
import com.ibm.mq.MQException;
import com.jcca.web.ibmMQ.command.ICommandProcessor;
import com.jcca.web.ibmMQ.diagnose.IDiagnosable;
import com.jcca.web.ibmMQ.domain.Monitor;
import com.jcca.web.ibmMQ.domain.StatisticalData;
import com.jcca.web.ibmMQ.support.IStatisticalDataProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.concurrent.ThreadSafe;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ThreadSafe
final class MonitorTask implements IDiagnosable, Runnable {
    private static final Logger log = LoggerFactory.getLogger(MonitorTask.class);

    private final IStatisticalDataProcessor dataProcessor;
    private final ICommandProcessor commandProcessor;
    private final Monitor monitor;
    private final Map<String, Object> context;
    private final Indicator indicator;


    public MonitorTask(final Monitor monitor, ICommandProcessor commandProcessor, IStatisticalDataProcessor dataProcessor) {
        Assert.notNull(monitor);
        Assert.notNull(commandProcessor);
        Assert.notNull(dataProcessor);

        this.monitor = monitor.clone();
        this.commandProcessor = commandProcessor;
        this.dataProcessor = dataProcessor;

        this.context = new HashMap<String, Object>(1) {
            {
                this.put("context.monitor", monitor);
                this.put("context.autoConnect", true);
            }
        };

        this.indicator = new Indicator();

    }

    public String getName() {
        return this.monitor.getConnection().getName() + "/" + this.monitor.getName();
    }

    public Monitor getMonitor() {
        return this.monitor;
    }

    public Indicator getIndicator() {
        return this.indicator;
    }


    public void run() {
        try {
            if (log.isDebugEnabled()) {
                log.debug("Monitor Task '{}' is collecting data for {} ...", getName(), this.monitor.getName());
            }
            List<StatisticalData> datas = (List<StatisticalData>) this.commandProcessor.process(this.monitor, this.context);
            this.indicator.increaseSuccessCount();
            for (StatisticalData data : datas) {
                this.dataProcessor.process(data);
            }
        } catch (MQException mqe) {
            int rc = mqe.getReason();
            if (rc == 2033) {
                this.indicator.increaseTimeoutCount();
                log.error("It might be a request timeout problem, no response received, retry later !", (Throwable) mqe);
            } else {
                this.indicator.increaseFailureCount();
         /*       log.error("It might be a connection issue or other error, reason: {}, retry later!",
                        MQConstants.lookupReasonCode(rc), mqe);*/
            }
        } catch (Throwable e) {
            log.error(String.format("Failed to get response for monitor: %s", new Object[]{this.monitor.getName()}), e);
            this.indicator.increaseFailureCount();
        }
    }

    public <A> A adapt(Class<A> type) {
        if (type == Monitor.class) {
            return (A) this.monitor;
        }
        return null;
    }

    public String toString() {
        return getName();
    }
}