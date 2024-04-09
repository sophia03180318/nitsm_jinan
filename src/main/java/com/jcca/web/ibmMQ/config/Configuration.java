package com.jcca.web.ibmMQ.config;


import com.jcca.web.ibmMQ.common.ErrorConstants;
import com.jcca.web.ibmMQ.common.Time;
import com.jcca.web.ibmMQ.health.IHealthEvaluator;
import com.jcca.web.ibmMQ.util.ArgumentChecker;
import com.jcca.web.ibmMQ.util.Strings;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.Serializable;


@org.springframework.context.annotation.Configuration
@ConfigurationProperties(prefix = "config")
@PropertySource("classpath:config.properties")
@Data
@Component
public class Configuration implements Serializable {
    private static final long serialVersionUID = -2197288193130953234L;
    private static final transient Logger log = LoggerFactory.getLogger(Configuration.class);
    @Resource(name = "healthEvaluator")
    private transient IHealthEvaluator healthEvaluator;


    private boolean connectionAutoStart;


    private String monitorPollingInterval;


    private String monitorPollingIntervalMin;

    private String monitorSlidingWindow;


    private String monitorSlidingWindowMax;

    private String monitorDataExpirationTime;

    private String monitorDataExpirationTimeMax;

    private int monitorConnectionTimeout;

    private int maxActiveMonitors;

    private long statisticsCacheSize;

    private String mqttMonitorPollingInterval;

    private String mqttMonitorSlidingWindow;

    private String mqttMonitorDataExpirationTime;

    private String qmgrMonitorPollingInterval;

    private String qmgrMonitorSlidingWindow;

    private String qmgrMonitorDataExpirationTime;

    private String channelHealthRule;

    private String queueHealthRule;

    private String topicHealthRule;

    private String listenerHealthRule;

    private String queueManagerHealthRule;

    private boolean createMonitorDefaultUser;

    private int numberOfDataWorkers;

    private int dataWorkerQueueCapacity;

    private float diagnoseFailRateThreshold;

    private float diagnoseTimeoutRateThreshold;

    private int diagnoseConsecutiveFailureThreshold;

    private int diagnosableThreshold;

    private String dataPurgeInterval;

    private String monitorDiagnoseInterval;

    private String mqttConnectionRefreshInterval;

    private String monitorDiagnoseStrategy;

    private String monitorScheduleStrategy;

    private String subscriptionDestinationCleanInterval;

    private String notificationProviders;


    @PostConstruct
    private void load() {
        validateAll();

    }

    private void validateAll() {
        try {
            Time.validateSMHD(this.dataPurgeInterval, formattedMessage("dataPurgeInterval", this.dataPurgeInterval));
            Time.validateSMHD(this.monitorDiagnoseInterval, formattedMessage("monitorDiagnoseInterval", this.monitorDiagnoseInterval));

            Time pollingIntervalTime = Time.parseSMHD(this.monitorPollingInterval,
                    formattedMessage("monitorPollingInterval", this.monitorPollingInterval));
            Time pollingIntervalTimeMin = Time.parseSMHD(this.monitorPollingIntervalMin,
                    formattedMessage("monitorPollingIntervalMin", this.monitorPollingIntervalMin));

            Time slidingWindowTime = Time.parseMHD(this.monitorSlidingWindow,
                    formattedMessage("monitorSlidingWindow", this.monitorSlidingWindow));
            Time slidingWindowTimeMax = Time.parseMHD(this.monitorSlidingWindowMax,
                    formattedMessage("monitorSlidingWindowMax", this.monitorSlidingWindowMax));

            Time dataExpirationTime = Time.parseMHD(this.monitorDataExpirationTime,
                    formattedMessage("monitorDataExpirationTime", this.monitorDataExpirationTime));
            Time dataExpirationTimeMax = Time.parseMHD(this.monitorDataExpirationTimeMax,
                    formattedMessage("monitorDataExpirationTimeMax", this.monitorDataExpirationTimeMax));

            ArgumentChecker.satisfy(pollingIntervalTime.is(Time.Operation.GE, pollingIntervalTimeMin), ErrorConstants.Message.MSG_INVALID_MONITOR_TIMEVALUE_1, new Object[]{pollingIntervalTime, pollingIntervalTimeMin});


            ArgumentChecker.satisfy(pollingIntervalTime.is(Time.Operation.LT, slidingWindowTime), ErrorConstants.Message.MSG_INVALID_MONITOR_TIMEVALUE_2, new Object[]{pollingIntervalTime, slidingWindowTime});


            ArgumentChecker.satisfy(slidingWindowTime.is(Time.Operation.LE, slidingWindowTimeMax), ErrorConstants.Message.MSG_INVALID_MONITOR_TIMEVALUE_3, new Object[]{slidingWindowTime, slidingWindowTimeMax});


            ArgumentChecker.satisfy(slidingWindowTime.is(Time.Operation.LT, dataExpirationTime), ErrorConstants.Message.MSG_INVALID_MONITOR_TIMEVALUE_4, new Object[]{slidingWindowTime, dataExpirationTime});

            ArgumentChecker.satisfy(dataExpirationTime.is(Time.Operation.LE, dataExpirationTimeMax), ErrorConstants.Message.MSG_INVALID_MONITOR_TIMEVALUE_5, new Object[]{dataExpirationTime, dataExpirationTimeMax});

            Time mqttMonitorPollingIntervalTime = Time.parseSMHD(this.mqttMonitorPollingInterval,
                    formattedMessage("mqttMonitorPollingInterval", this.mqttMonitorPollingInterval));
            Time mqttMonitorSlidingWindowTime = Time.parseMHD(this.mqttMonitorSlidingWindow,
                    formattedMessage("mqttMonitorSlidingWindow", this.mqttMonitorSlidingWindow));
            Time mqttDataExpirationTime = Time.parseMHD(this.mqttMonitorDataExpirationTime,
                    formattedMessage("mqttMonitorDataExpirationTime", this.mqttMonitorDataExpirationTime));

            ArgumentChecker.satisfy(mqttMonitorPollingIntervalTime.is(Time.Operation.GE, pollingIntervalTimeMin), ErrorConstants.Message.MSG_INVALID_MONITOR_TIMEVALUE_1, new Object[]{mqttMonitorPollingIntervalTime, pollingIntervalTimeMin});


            ArgumentChecker.satisfy(mqttMonitorPollingIntervalTime.is(Time.Operation.LT, mqttMonitorSlidingWindowTime), ErrorConstants.Message.MSG_INVALID_MONITOR_TIMEVALUE_2, new Object[]{mqttMonitorPollingIntervalTime, mqttMonitorSlidingWindowTime});

            ArgumentChecker.satisfy(mqttMonitorSlidingWindowTime.is(Time.Operation.LE, slidingWindowTimeMax), ErrorConstants.Message.MSG_INVALID_MONITOR_TIMEVALUE_3, new Object[]{mqttMonitorSlidingWindowTime, slidingWindowTimeMax});


            ArgumentChecker.satisfy(mqttMonitorSlidingWindowTime.is(Time.Operation.LT, mqttDataExpirationTime), ErrorConstants.Message.MSG_INVALID_MONITOR_TIMEVALUE_4, new Object[]{mqttMonitorSlidingWindowTime, mqttDataExpirationTime});


            ArgumentChecker.satisfy(mqttDataExpirationTime.is(Time.Operation.LE, dataExpirationTimeMax), ErrorConstants.Message.MSG_INVALID_MONITOR_TIMEVALUE_5, new Object[]{mqttDataExpirationTime, dataExpirationTimeMax});

            Time qmgrMonitorPollingIntervalTime = Time.parseSMHD(this.qmgrMonitorPollingInterval,
                    formattedMessage("qmgrMonitorPollingInterval", this.qmgrMonitorPollingInterval));
            Time qmgrMonitorSlidingWindowTime = Time.parseMHD(this.qmgrMonitorSlidingWindow,
                    formattedMessage("qmgrMonitorSlidingWindow", this.qmgrMonitorSlidingWindow));
            Time qmgrDataExpirationTime = Time.parseMHD(this.qmgrMonitorDataExpirationTime,
                    formattedMessage("qmgrMonitorDataExpirationTime", this.qmgrMonitorDataExpirationTime));

            ArgumentChecker.satisfy(qmgrMonitorPollingIntervalTime.is(Time.Operation.GE, pollingIntervalTimeMin), ErrorConstants.Message.MSG_INVALID_MONITOR_TIMEVALUE_1, new Object[]{qmgrMonitorPollingIntervalTime, pollingIntervalTimeMin});


            ArgumentChecker.satisfy(qmgrMonitorPollingIntervalTime.is(Time.Operation.LT, qmgrMonitorSlidingWindowTime), ErrorConstants.Message.MSG_INVALID_MONITOR_TIMEVALUE_2, new Object[]{qmgrMonitorPollingIntervalTime, qmgrMonitorSlidingWindowTime});

            ArgumentChecker.satisfy(qmgrMonitorSlidingWindowTime.is(Time.Operation.LE, slidingWindowTimeMax), ErrorConstants.Message.MSG_INVALID_MONITOR_TIMEVALUE_3, new Object[]{qmgrMonitorSlidingWindowTime, slidingWindowTimeMax});


            ArgumentChecker.satisfy(qmgrMonitorSlidingWindowTime.is(Time.Operation.LT, qmgrDataExpirationTime), ErrorConstants.Message.MSG_INVALID_MONITOR_TIMEVALUE_4, new Object[]{qmgrMonitorSlidingWindowTime, qmgrDataExpirationTime});


            ArgumentChecker.satisfy(qmgrDataExpirationTime.is(Time.Operation.LE, dataExpirationTimeMax), ErrorConstants.Message.MSG_INVALID_MONITOR_TIMEVALUE_5, new Object[]{qmgrDataExpirationTime, dataExpirationTimeMax});


            if (!Strings.isNullOrEmpty(this.channelHealthRule)) {
                this.healthEvaluator.validate(this.channelHealthRule);
            }
            if (!Strings.isNullOrEmpty(this.queueHealthRule)) {
                this.healthEvaluator.validate(this.queueHealthRule);
            }
            if (!Strings.isNullOrEmpty(this.topicHealthRule)) {
                this.healthEvaluator.validate(this.topicHealthRule);
            }
            if (!Strings.isNullOrEmpty(this.listenerHealthRule)) {
                this.healthEvaluator.validate(this.listenerHealthRule);
            }
            if (!Strings.isNullOrEmpty(this.queueManagerHealthRule)) {
                this.healthEvaluator.validate(this.queueManagerHealthRule);
            }
        } catch (Exception e) {

            throw new InvalidConfigurationException(40008, ErrorConstants.Message.MSG_INVALID_CONFIGURATION, new Object[]{e});
        }
    }


    private String formattedMessage(String property, String value) {
        return String.format("%s = %s", new Object[]{property, value});
    }
}
