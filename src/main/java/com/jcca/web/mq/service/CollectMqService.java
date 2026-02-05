package com.jcca.web.mq.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jcca.web.mq.entity.CollectMq;

/**
 * @ Author：sophia
 * @ Date：Created in 15:14 2021/11/18
 * @ Description:
 */
public interface CollectMqService extends IService<CollectMq> {
    public void removeCollectData(String connectionId);

}
