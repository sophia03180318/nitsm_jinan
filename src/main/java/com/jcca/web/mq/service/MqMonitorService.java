package com.jcca.web.mq.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jcca.web.mq.entity.MqMonitor;

import java.util.List;

/**
 * @ Author：sophia
 * @ Date：Created in 15:14 2021/11/18
 * @ Description:
 */
public interface MqMonitorService extends IService<MqMonitor> {


    List<MqMonitor> getMonitorByGroupId(String groupId);

    List<MqMonitor> getQueueByConnectionId(String connectionId);

    List<MqMonitor> getChannelByConnectionId(String connectionId);

}
