package com.jcca.web.ibmMQ.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jcca.web.ibmMQ.entity.IBMGroup;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @ Author：sophia
 * @ Date：Created in 11:46 2021/11/25
 * @ Description:
 */
public interface IBMGroupMapper extends BaseMapper<IBMGroup> {
    @Select("select * from IBMMQ_GROUP where name=#{name} and CONNECT_ID=#{connectId}")
    List<IBMGroup> selectByName(String name, String connectId);
}
