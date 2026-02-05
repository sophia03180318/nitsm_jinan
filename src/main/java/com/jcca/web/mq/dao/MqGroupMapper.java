package com.jcca.web.mq.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jcca.web.mq.entity.MqGroup;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @ Author：sophia
 * @ Date：Created in 11:46 2021/11/25
 * @ Description:
 */
public interface MqGroupMapper extends BaseMapper<MqGroup> {
    @Select("select * from MQ_GROUP where name = #{name} and CONNECT_ID = #{connectId}")
    List<MqGroup> selectByName(String name, String connectId);

    @Select("select * from MQ_GROUP where CONNECT_ID = #{connectId}")
    List<MqGroup> selectByConnectId( String connectId);
}
