package com.jcca.web2.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web2.entity.BusinessServiceType;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @description: 业务类型
 * @author: sophia
 * @create: 2023/11/01 10:20
 **/
public interface BusinessServiceTypeMapper extends BaseMapper<BusinessServiceType> {

    @Select("select * FROM ASSET ")
    List<Asset> getAssetByType(String orgId, String type);

    @Select("select max(sort) from BUSINESSSERVICE_SERVICE_TYPE ")
    Integer getMaxSort();

    @Select("select id from BUSINESSSERVICE_SERVICE_TYPE where name = #{name}")
    List<String> getTypeByName(String name);
}