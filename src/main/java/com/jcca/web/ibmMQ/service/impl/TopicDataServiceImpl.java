package com.jcca.web.ibmMQ.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcca.web.ibmMQ.dao.TopicDataMapper;
import com.jcca.web.ibmMQ.entity.IBMTopicData;
import com.jcca.web.ibmMQ.service.TopicDataService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author zhaozheng
 * @date 2020-07-14 15:00
 **/
@Service
public class TopicDataServiceImpl extends ServiceImpl<TopicDataMapper, IBMTopicData> implements TopicDataService {
    @Resource
    private TopicDataMapper topicDataMapper;

    @Override
    public IBMTopicData queryLastDate(String monitorId) {
        return topicDataMapper.queryLastDate(monitorId);
    }

    @Override
    public List<IBMTopicData> queryDetailData(String monitorId) {
        return topicDataMapper.queryDetailData(monitorId);
    }

    @Override
    public Boolean removeStatistics(String monitorId) {
        return topicDataMapper.removeStatistics(monitorId);
    }
}