package com.jcca.web.ibmMQ.dao;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jcca.web.ibmMQ.domain.Connection;
import com.jcca.web.ibmMQ.entity.IBMConnection;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author hanwone
 * @date 2020-07-14 15:56:23
 **/
public interface ConnectionMapper extends BaseMapper<IBMConnection> {

    List<Connection> findConnections();

    @Select("select * from IBMMQ_QMGR_CONNECTION a where a.connect_name = #{connectionName}")
    List<IBMConnection> findByName(@Param("connectionName") String connectionName);

    @Select("select * from IBMMQ_QMGR_CONNECTION where connect_host = #{host} and connect_port= #{port}")
    List<IBMConnection> findByHostAndPort(@Param("host") String host, @Param("port") Integer port);


}
