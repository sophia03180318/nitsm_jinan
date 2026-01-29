package com.jcca.web.mq.dao;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jcca.web.mq.entity.MqConnection;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author hanwone
 * @date 2020-07-14 15:56:23
 **/
public interface MqConnectionMapper extends BaseMapper<MqConnection> {

    List<MqConnection> findConnections();

    List<MqConnection> findByName(@Param("connectionName") String connectionName);

    List<MqConnection> findByHostAndPort(@Param("host") String host, @Param("port") Integer port);


}
