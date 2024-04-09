package com.jcca.web.ibmMQ.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcca.web.ibmMQ.dao.IBMMonitorMapper;
import com.jcca.web.ibmMQ.entity.IBMMonitor;
import com.jcca.web.ibmMQ.service.IBMMonitorService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @ Author：sophia
 * @ Date：Created in 15:21 2021/11/18
 * @ Description:
 */
@Service
public class IBMMonitorImpl extends ServiceImpl<IBMMonitorMapper, IBMMonitor> implements IBMMonitorService {
    @Resource
    IBMMonitorMapper ibmMonitorMapper;

    @Override
    public IBMMonitor getQueueStatus(String monitorId) {
        List<IBMMonitor> queueStatus = ibmMonitorMapper.getQueueStatus(monitorId);
        if (queueStatus.size() < 1) {
            return new IBMMonitor();
        }
        return queueStatus.get(0);
    }

    @Override
    public IBMMonitor getChannelStatus(String monitorId) {
        List<IBMMonitor> channelStatus = ibmMonitorMapper.getChannelStatus(monitorId);
        if (channelStatus.size() < 1) {
            return new IBMMonitor();
        }
        return channelStatus.get(0);
    }

    @Override
    public List<IBMMonitor> getMonitorById(String groupId) {
        return ibmMonitorMapper.getMonitorById(groupId);

    }


}
