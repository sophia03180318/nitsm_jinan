package com.jcca.web.ibmMQ.dao;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jcca.web.ibmMQ.entity.IBMStatisticalData;
import org.apache.ibatis.annotations.Delete;

import java.util.Date;

/**
 * @author zhaozheng
 * @date 2020-07-14 14:59:12
 **/
public interface StatisticalDataMapper extends BaseMapper<IBMStatisticalData> {

    Boolean removeExpiredStatistics(String monitorId, Date captureTime);


}
