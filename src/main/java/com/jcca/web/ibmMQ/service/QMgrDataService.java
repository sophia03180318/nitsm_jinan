package com.jcca.web.ibmMQ.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.jcca.web.ibmMQ.entity.IBMQMgrData;

/**
 * @author zhaozheng
 * @date 2020-07-14 14:48:52
 **/
public interface QMgrDataService extends IService<IBMQMgrData> {
    IBMQMgrData queryLastDate(String monitorId);

    Boolean removeStatistics(String monitorId);

}
