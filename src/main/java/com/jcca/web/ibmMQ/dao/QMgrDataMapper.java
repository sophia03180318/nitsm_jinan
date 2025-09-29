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

    Boolean removeExpiredStatistics(String monitorId, Date captureTime);

    IBMQMgrData queryLastDate(String monitorId);

    Boolean removeStatistics(String monitorId);

}
