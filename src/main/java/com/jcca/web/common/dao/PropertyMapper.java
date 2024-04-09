package com.jcca.web.common.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jcca.web.common.entity.Property;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @description:
 * @author: sophia
 * @create: 2023/11/30 14:11
 **/
public interface PropertyMapper extends BaseMapper<Property> {

    @Select("select * from DH_PROPERTY where  CREATE_TIME = (select max(CREATE_TIME) from DH_PROPERTY)")
    List<Property> selectProperty();


    @Select("select * from DH_PROPERTY where (NAME like '%温度' or NAME like '%湿度' ) and CREATE_TIME = (select max(CREATE_TIME) from DH_PROPERTY)")
    List<Property> selectThreeDProperty();
}