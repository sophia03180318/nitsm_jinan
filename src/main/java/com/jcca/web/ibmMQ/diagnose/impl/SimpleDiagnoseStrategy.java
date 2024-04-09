/*     */
package com.jcca.web.ibmMQ.diagnose.impl;


import com.jcca.web.ibmMQ.config.Configuration;
import com.jcca.web.ibmMQ.diagnose.IDiagnosable;
import com.jcca.web.ibmMQ.diagnose.IDiagnoseStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;


@Component("simpleDiagnoseStrategy")
public class SimpleDiagnoseStrategy implements IDiagnoseStrategy {
    private static final Logger log = LoggerFactory.getLogger(SimpleDiagnoseStrategy.class);

    private float failRateThreshold = 0.2F;
    private float timeoutRateThreshold = 0.4F;

    private int consecutiveFailureThreshold = 20;
    private int diagnosableThreshold = 10;
    @Resource(name = "configuration")
    private Configuration configuration;

    @PostConstruct
    private void init() {
        if (this.configuration.getDiagnosableThreshold() > 0) {
            this.diagnosableThreshold = this.configuration.getDiagnosableThreshold();
        }
        if (this.configuration.getDiagnoseConsecutiveFailureThreshold() > 0) {
            this.consecutiveFailureThreshold = this.configuration.getDiagnoseConsecutiveFailureThreshold();
        }
        if (this.configuration.getDiagnoseFailRateThreshold() > 0.0F) {
            this.failRateThreshold = this.configuration.getDiagnoseFailRateThreshold();
        }
        if (this.configuration.getDiagnoseTimeoutRateThreshold() > 0.0F) {
            this.timeoutRateThreshold = this.configuration.getDiagnoseTimeoutRateThreshold();
        }
        if (log.isDebugEnabled()) {
            log.debug("Init with diagnose configuration: diagnosableThreshold={},consecutiveFailureThreshold={},failRateThreshold={},timeoutRateThreshold={}", new Object[]{

                    Integer.valueOf(this.diagnosableThreshold), Integer.valueOf(this.consecutiveFailureThreshold), Float.valueOf(this.failRateThreshold),
                    Float.valueOf(this.timeoutRateThreshold)
            });
        }
    }

    public String getName() {
        return "default";
    }

    //评估是否要将监控的MQ对象删除掉，qmr不会被删除，其他的通过失败率和期望失败次数删除任务
    @Override
    public boolean isHealthy(IDiagnosable diagnosable) {
        IDiagnosable.Indicator indicator = diagnosable.getIndicator();
        if (log.isDebugEnabled()) {
            log.debug("Health indicator {}", indicator);
        }
        int consecutiveFailureCount = indicator.getConsecutiveFailureCount();
        int fail = indicator.getFailureCount();
        int success = indicator.getSuccessCount();
        int timeout = indicator.getTimeoutCount();
        int total = fail + success + timeout;
        if (total < this.diagnosableThreshold) {
            return true;
        }
        float timeoutRate = timeout * 1.0F / total;
        float failRate = fail * 1.0F / total;
        if (log.isDebugEnabled()) {
            log.debug("Diagnose result: timeoutRate={}, failRate={}, consecutiveFailureCount={}", new Object[]{
                    Float.valueOf(timeoutRate), Float.valueOf(failRate), Integer.valueOf(consecutiveFailureCount)
            });
        }
        return ((timeoutRate < this.timeoutRateThreshold && failRate < this.failRateThreshold) || consecutiveFailureCount >= this.consecutiveFailureThreshold);
    }

}

