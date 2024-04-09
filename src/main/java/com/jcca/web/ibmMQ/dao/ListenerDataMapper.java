package com.jcca.web.ibmMQ.dao;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jcca.web.ibmMQ.entity.IBMListenerData;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Select;

import java.util.Date;
import java.util.List;

/**
 * @author hanwone
 * @date 2020-07-14 16:09:21
 **/
public interface ListenerDataMapper extends BaseMapper<IBMListenerData> {

    @Delete("delete from IBMMQ_LISTENER_DATA where monitor_id=#{monitorId} and captureTime<#{captureTime}")
    Boolean removeExpiredStatistics(String monitorId, Date captureTime);

    @Select("select * from (select * from IBMMQ_LISTENER_DATA where monitor_id=#{monitorId}  order by captureTime desc) t where rownum=1")
    IBMListenerData queryLastDate(String monitorId);


    @Select("select * from (select * from IBMMQ_LISTENER_DATA where monitor_id=#{monitorId}  order by captureTime desc) t where  rownum<=50")
    List<IBMListenerData> queryDetailData(String monitorId);

    @Delete("delete from IBMMQ_LISTENER_DATA where monitor_id=#{monitorId}")
    Boolean removeStatistics(String monitorId);

}
