package com.jcca.web.ibmMQ.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jcca.web.ibmMQ.entity.IBMChannelData;

import java.util.List;

/**
 * @author zhaozheng
 * @date 2020-07-14 15:53:25
 **/
public interface ChannelDataService extends IService<IBMChannelData> {
    IBMChannelData queryLastDate(String monitorId);
//    void saveChannelData(Connection paramConnection, List<MQTTChannelData> paramList);

    List<IBMChannelData> queryDetailData(String monitorId);

    Boolean removeStatistics(String monitorId);

}
