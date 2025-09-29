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
    List<IBMMonitor> getQueueStatus(String monitorId);

    List<IBMMonitor> getChannelStatus(String monitorId);

    List<IBMMonitor> getMonitorById(String groupId);

}
