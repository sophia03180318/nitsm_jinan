package com.jcca.web.ibmMQ.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jcca.web.ibmMQ.entity.IBMMonitor;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @ Author：sophia
 * @ Date：Created in 15:23 2021/11/18
 * @ Description:
 */
public interface IBMMonitorMapper extends BaseMapper<IBMMonitor> {
    @Select("select * from IBMMQ_QUEUE_DATA where MONITOR_ID=#{monitorId} AND CAPTURETIME = (select max(CAPTURETIME) from IBMMQ_QUEUE_DATA where MONITOR_ID=#{monitorId})")
    List<IBMMonitor> getQueueStatus(String monitorId);

    @Select("select * from IBMMQ_CHANNEL_DATA where MONITOR_ID=#{monitorId} AND CAPTURETIME = (select max(CAPTURETIME) from IBMMQ_CHANNEL_DATA where MONITOR_ID=#{monitorId})")
    List<IBMMonitor> getChannelStatus(String monitorId);

    @Select("select * from IBMMQ_MONITOR where GROUP_ID=#{groupId}")
    List<IBMMonitor> getMonitorById(String groupId);

}
