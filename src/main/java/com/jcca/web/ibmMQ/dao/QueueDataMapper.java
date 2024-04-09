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

    @Delete("delete from IBMMQ_QUEUE_DATA where monitor_id=#{monitorId} and captureTime<#{captureTime}")
    Boolean removeExpiredStatistics(String monitorId, Date captureTime);

    @Select("select * from (select * from IBMMQ_QUEUE_DATA where monitor_id=#{monitorId}  order by captureTime desc) t where rownum=1")
    IBMQueueData queryLastDate(String monitorId);


    @Select("select * from (select * from IBMMQ_QUEUE_DATA where monitor_id=#{monitorId}  order by captureTime desc) t where  rownum<=50")
    List<IBMQueueData> queryDetailData(String monitorId);

    @Delete("delete from IBMMQ_QUEUE_DATA where monitor_id=#{monitorId}")
    Boolean removeStatistics(String monitorId);


}
