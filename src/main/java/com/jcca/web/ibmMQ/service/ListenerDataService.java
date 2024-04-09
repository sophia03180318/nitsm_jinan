package com.jcca.web.ibmMQ.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jcca.web.ibmMQ.entity.IBMListenerData;

import java.util.List;

/**
 * @author zhaozheng
 * @date 2020-07-14 16:09:21
 **/
public interface ListenerDataService extends IService<IBMListenerData> {

    IBMListenerData queryLastDate(String monitorId);

    List<IBMListenerData> queryDetailData(String monitorId);

    Boolean removeStatistics(String monitorId);
}
