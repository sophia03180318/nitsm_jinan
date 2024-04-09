package com.jcca.web.ibmMQ.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcca.web.ibmMQ.dao.IBMMeasurementsMapper;
import com.jcca.web.ibmMQ.entity.IBMMeasurements;
import com.jcca.web.ibmMQ.service.IBMMeasurementsService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author zhaozheng
 * @date 2020-07-28 09:49
 **/
@Service
public class IBMMeasurementsServiceImpl extends ServiceImpl<IBMMeasurementsMapper, IBMMeasurements> implements IBMMeasurementsService {
    @Resource
    private IBMMeasurementsMapper ibmMeasurementsMapper;

    @Override
    public Boolean deleteMeasurements(String monitorId) {
        return ibmMeasurementsMapper.deleteMeasurements(monitorId);
    }
}