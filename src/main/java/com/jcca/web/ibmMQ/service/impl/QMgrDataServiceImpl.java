package com.jcca.web.ibmMQ.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcca.web.ibmMQ.dao.QMgrDataMapper;
import com.jcca.web.ibmMQ.entity.IBMQMgrData;
import com.jcca.web.ibmMQ.service.QMgrDataService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author zhaozheng
 * @date 2020-07-14 14:48
 **/
@Service
public class QMgrDataServiceImpl extends ServiceImpl<QMgrDataMapper, IBMQMgrData> implements QMgrDataService {
    @Resource
    private QMgrDataMapper qMgrDataMapper;

    @Override
    public IBMQMgrData queryLastDate(String monitorId) {
        return qMgrDataMapper.queryLastDate(monitorId);
    }

    @Override
    public Boolean removeStatistics(String monitorId) {
        return qMgrDataMapper.removeStatistics(monitorId);
    }
}