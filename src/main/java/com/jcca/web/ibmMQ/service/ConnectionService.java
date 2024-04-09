package com.jcca.web.ibmMQ.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.jcca.web.ibmMQ.domain.Connection;
import com.jcca.web.ibmMQ.entity.IBMConnection;

import java.util.List;

/**
 * @author zhaozheng
 * @date 2020-07-14 15:56:23
 **/
public interface ConnectionService extends IService<IBMConnection> {

    public List<Connection> listConnections();

    public Connection createConnection(Connection connection);

    public List<IBMConnection> findByName(String connectionName);

    public List<IBMConnection> findByHostAndPort(String host, Integer port);

    public Connection findConnection(String connectionName);


    public void removeConnection(String connectionId, String connectionName);
}
