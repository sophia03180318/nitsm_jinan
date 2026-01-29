package com.jcca.web.mq.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcca.web.mq.dao.MqMonitorMapper;
import com.jcca.web.mq.entity.MqMonitor;
import com.jcca.web.mq.service.MqMonitorService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @ Author：sophia
 * @ Date：Created in 15:21 2021/11/18
 * @ Description:
 */
@Service
public class MqonitorImpl extends ServiceImpl<MqMonitorMapper, MqMonitor> implements MqMonitorService {
    @Resource
    MqMonitorMapper mqMonitorMapper;

    @Override
    public List<MqMonitor> getQueueByGroupId(String groupId) {
        return mqMonitorMapper.getQueueByGroupId(groupId);
    }

    @Override
    public List<MqMonitor> getChannelByGroupId(String groupId) {
        return mqMonitorMapper.getChannelByGroupId(groupId);
    }


    @Override
    public List<MqMonitor> getChannelByConnectionId(String groupId) {
        return mqMonitorMapper.getChannelByConnectionId(groupId);

    }

    @Override
    public List<MqMonitor> getQueueByConnectionId(String connectionId) {
        return mqMonitorMapper.getQueueByConnectionId(connectionId);

    }


}
