package com.jcca.web.ibmMQ.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jcca.web.ibmMQ.entity.IBMTopicData;

import java.util.List;

/**
 * @author zhaozheng
 * @date 2020-07-14 15:00:55
 **/
public interface TopicDataService extends IService<IBMTopicData> {
    IBMTopicData queryLastDate(String monitorId);

    List<IBMTopicData> queryDetailData(String monitorId);

    Boolean removeStatistics(String monitorId);

}
