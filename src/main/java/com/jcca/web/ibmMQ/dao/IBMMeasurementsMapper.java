package com.jcca.web.ibmMQ.dao;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jcca.web.ibmMQ.entity.IBMMeasurements;
import org.apache.ibatis.annotations.Delete;

/**
 * @author zhaozheng
 * @date 2020-07-28 09:49:20
 **/
public interface IBMMeasurementsMapper extends BaseMapper<IBMMeasurements> {

    @Delete("delete from IBMMQ_MONITOR_MEASUREMENTS where MONITOR_ID=#{monitorId}")
    Boolean deleteMeasurements(String monitorId);


}
