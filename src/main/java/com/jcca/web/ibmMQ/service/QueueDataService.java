package com.jcca.web.ibmMQ.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jcca.web.ibmMQ.entity.IBMQueueData;

import java.util.List;

/**
 * @author zhaozheng
 * @date 2020-07-14 14:53:08
 **/
public interface QueueDataService extends IService<IBMQueueData> {

    IBMQueueData queryLastDate(String monitorId);

    List<IBMQueueData> queryDetailData(String monitorId);

    Boolean removeStatistics(String monitorId);
}
