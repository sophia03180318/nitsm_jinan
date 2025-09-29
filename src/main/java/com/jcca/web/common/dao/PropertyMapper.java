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

    List<Property> selectProperty();


    List<Property> selectThreeDProperty();
}