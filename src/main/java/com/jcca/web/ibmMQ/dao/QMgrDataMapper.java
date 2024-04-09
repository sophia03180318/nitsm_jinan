package com.jcca.web.ibmMQ.dao;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jcca.web.ibmMQ.entity.IBMQMgrData;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Select;

import java.util.Date;

/**
 * @author zhaozheng
 * @date 2020-07-14 14:48:52
 **/
public interface QMgrDataMapper extends BaseMapper<IBMQMgrData> {

    @Delete("delete from IBMMQ_QMGR_DATA where monitor_id=#{monitorId} and captureTime<#{captureTime}")
    Boolean removeExpiredStatistics(String monitorId, Date captureTime);

    @Select("select * from (select * from IBMMQ_QMGR_DATA where monitor_id=#{monitorId}  order by captureTime desc) t where rownum=1")
    IBMQMgrData queryLastDate(String monitorId);


    @Delete("delete from IBMMQ_QMGR_DATA where monitor_id=#{monitorId}")
    Boolean removeStatistics(String monitorId);

}
