package com.jcca.web.ibmMQ.support;


import com.jcca.web.ibmMQ.domain.Connection;

public interface IConnectionListener {
    void afterConnectionUpdated(Connection paramConnection1, Connection paramConnection2);

    void beforeRemovingConnection(Connection paramConnection);

    void afterConnectionRemoved(Connection paramConnection);
}

