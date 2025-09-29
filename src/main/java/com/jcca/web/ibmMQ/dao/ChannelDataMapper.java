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
    Boolean removeExpiredStatistics(String monitorId, Date captureTime);

    IBMChannelData queryLastDate(String monitorId);


    List<IBMChannelData> queryDetailData(String monitorId);

    Boolean removeStatistics(String monitorId);


}
