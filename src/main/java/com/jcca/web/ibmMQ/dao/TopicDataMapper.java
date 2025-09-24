package com.jcca.web.ibmMQ.dao;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jcca.web.ibmMQ.entity.IBMTopicData;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Select;

import java.util.Date;
import java.util.List;

/**
 * @author hanwone
 * @date 2020-07-14 15:00:55
 **/
public interface TopicDataMapper extends BaseMapper<IBMTopicData> {

    Boolean removeExpiredStatistics(String monitorId, Date captureTime);

    IBMTopicData queryLastDate(String monitorId);

    List<IBMTopicData> queryDetailData(String monitorId);

    Boolean removeStatistics(String monitorId);
}
