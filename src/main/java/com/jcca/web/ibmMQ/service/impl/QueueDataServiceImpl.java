package com.jcca.web.ibmMQ.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcca.web.ibmMQ.dao.QueueDataMapper;
import com.jcca.web.ibmMQ.entity.IBMQueueData;
import com.jcca.web.ibmMQ.service.QueueDataService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author zhaozheng
 * @date 2020-07-14 14:53
 **/
@Service
public class QueueDataServiceImpl extends ServiceImpl<QueueDataMapper, IBMQueueData> implements QueueDataService {
    @Resource
    private QueueDataMapper queueDataMapper;

    @Override
    public IBMQueueData queryLastDate(String monitorId) {
        return queueDataMapper.queryLastDate(monitorId);
    }

    @Override
    public List<IBMQueueData> queryDetailData(String monitorId) {
        return queueDataMapper.queryDetailData(monitorId);
    }

    @Override
    public Boolean removeStatistics(String monitorId) {
        return queueDataMapper.removeStatistics(monitorId);
    }
}