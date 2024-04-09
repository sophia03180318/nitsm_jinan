package com.jcca.web.ibmMQ.support;


import com.jcca.web.ibmMQ.domain.Connection;

public class ConnectionAdapter implements IConnectionListener {


    public void beforeRemovingConnection(Connection connection) {
    }

    public void afterConnectionRemoved(Connection connection) {
    }

    @Override
    public void afterConnectionUpdated(Connection paramConnection1, Connection paramConnection2) {

    }
}
