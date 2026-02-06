package com.jcca.web.mq.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcca.web.mq.dao.CollectMqMapper;
import com.jcca.web.mq.entity.CollectMq;
import com.jcca.web.mq.service.CollectMqService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @description:
 * @author: sophia
 * @create: 2026/01/29 09:31
 **/
@Service
public class CollectMqServiceImpl extends ServiceImpl<CollectMqMapper, CollectMq> implements CollectMqService {
   @Resource
   private CollectMqMapper mapper;
    @Override
    public void removeCollectData(String connectionId) {
        mapper.removeCollectData(connectionId);
    }
}