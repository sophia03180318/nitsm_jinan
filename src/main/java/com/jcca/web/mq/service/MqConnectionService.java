package com.jcca.web.mq.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.jcca.web.mq.entity.MqConnection;

/**
 * @author zhaozheng
 * @date 2020-07-14 15:56:23
 **/
public interface MqConnectionService extends IService<MqConnection> {

    public void removeConnection(String connectionId, String connectionName);
}
