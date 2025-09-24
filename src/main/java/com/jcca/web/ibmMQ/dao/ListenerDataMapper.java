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

    Boolean removeExpiredStatistics(String monitorId, Date captureTime);

    IBMListenerData queryLastDate(String monitorId);


    List<IBMListenerData> queryDetailData(String monitorId);

    Boolean removeStatistics(String monitorId);

}
