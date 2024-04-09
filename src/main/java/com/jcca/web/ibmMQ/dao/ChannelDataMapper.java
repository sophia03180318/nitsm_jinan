package com.jcca.web.ibmMQ.dao;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jcca.web.ibmMQ.entity.IBMChannelData;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Select;

import java.util.Date;
import java.util.List;

/**
 * @author zhaozheng
 * @date 2020-07-14 15:53:25
 **/
public interface ChannelDataMapper extends BaseMapper<IBMChannelData> {
    @Delete("delete from IBMMQ_CHANNEL_DATA where monitor_id=#{monitorId} and captureTime<#{captureTime}")
    Boolean removeExpiredStatistics(String monitorId, Date captureTime);

    @Select("select * from (select * from IBMMQ_CHANNEL_DATA where monitor_id=#{monitorId}  order by captureTime desc) t where rownum=1")
    IBMChannelData queryLastDate(String monitorId);


    @Select("select * from (select * from IBMMQ_CHANNEL_DATA where monitor_id=#{monitorId}  order by captureTime desc) t where  rownum<=50")
    List<IBMChannelData> queryDetailData(String monitorId);

    @Delete("delete from IBMMQ_CHANNEL_DATA where monitor_id=#{monitorId}")
    Boolean removeStatistics(String monitorId);


}
