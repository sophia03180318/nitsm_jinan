package com.jcca.web.ibmMQ.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jcca.web.ibmMQ.entity.IBMMonitor;

import java.util.List;

/**
 * @ Author：sophia
 * @ Date：Created in 15:14 2021/11/18
 * @ Description:
 */
public interface IBMMonitorService extends IService<IBMMonitor> {
    IBMMonitor getQueueStatus(String monitorId);

    IBMMonitor getChannelStatus(String monitorId);

    List<IBMMonitor> getMonitorById(String groupId);

}
