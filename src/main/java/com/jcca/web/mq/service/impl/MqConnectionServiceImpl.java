package com.jcca.web.mq.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcca.web.mq.dao.MqConnectionMapper;
import com.jcca.web.mq.entity.MqConnection;
import com.jcca.web.mq.service.*;
import org.springframework.stereotype.Service;

/**
 * @author zhaozheng
 * @date 2020-07-14 15:56
 **/
@Service
public class MqConnectionServiceImpl extends ServiceImpl<MqConnectionMapper, MqConnection> implements MqConnectionService {

    @Override
    public void removeConnection(String connectionId, String connectionName) {

    }
}