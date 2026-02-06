package com.jcca.web.mq.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jcca.web.mq.entity.MqMonitor;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @ Author：sophia
 * @ Date：Created in 15:23 2021/11/18
 * @ Description:
 */
public interface MqMonitorMapper extends BaseMapper<MqMonitor> {

    @Select("select * from MQ_MONITOR where category like 'MQQT%' and CONNECT_ID=#{connectionId}")
    List<MqMonitor> getQueueByConnectionId(String connectionId);

    @Select("select * from MQ_MONITOR where category like '%CHANNEL' and CONNECT_ID=#{connectionId}")
    List<MqMonitor> getChannelByConnectionId(String connectionId);

    @Select("select * from MQ_MONITOR where GROUP_ID=#{groupId}")
    List<MqMonitor> getMonitorByGroupId(String groupId);
}
