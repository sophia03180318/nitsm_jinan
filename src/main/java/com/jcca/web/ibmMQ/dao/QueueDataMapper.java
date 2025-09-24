package com.jcca.web.ibmMQ.dao;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jcca.web.ibmMQ.entity.IBMQueueData;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Select;

import java.util.Date;
import java.util.List;

/**
 * @author zhaozheng
 * @date 2020-07-14 14:53:08
 **/
public interface QueueDataMapper extends BaseMapper<IBMQueueData> {

    Boolean removeExpiredStatistics(String monitorId, Date captureTime);

    IBMQueueData queryLastDate(String monitorId);

    List<IBMQueueData> queryDetailData(String monitorId);

    Boolean removeStatistics(String monitorId);


}
