package com.jcca.web.ibmMQ.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jcca.web.ibmMQ.entity.IBMMeasurements;

/**
 * @author zhaozheng
 * @date 2020-07-28 09:49:20
 **/
public interface IBMMeasurementsService extends IService<IBMMeasurements> {

    Boolean deleteMeasurements(String monitorId);
}
