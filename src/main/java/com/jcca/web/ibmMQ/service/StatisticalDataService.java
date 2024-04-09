package com.jcca.web.ibmMQ.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jcca.web.ibmMQ.domain.Monitor;
import com.jcca.web.ibmMQ.domain.StatisticalData;
import com.jcca.web.ibmMQ.entity.IBMStatisticalData;

/**
 * @author zhaozheng
 * @date 2020-07-14 14:59:12
 **/
public interface StatisticalDataService extends IService<IBMStatisticalData> {
    void removeExpiredStatistics(Monitor paramMonitor);

    void saveStatisticalData(StatisticalData paramStatisticalData);

    StatisticalData findLatestStatisticalData(Monitor monitor);

    StatisticalData queryMonitorDetails(Monitor monitor);


}
