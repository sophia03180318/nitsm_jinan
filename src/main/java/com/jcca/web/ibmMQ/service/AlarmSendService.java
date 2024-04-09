package com.jcca.web.ibmMQ.service;

import com.jcca.web.ibmMQ.domain.StatisticalData;

/**
 * @author zhaozheng@jccatech.com
 * @date 2020/7/31 16:07
 */
public interface AlarmSendService {
    void sendAlarm(StatisticalData paramStatisticalData);
}
