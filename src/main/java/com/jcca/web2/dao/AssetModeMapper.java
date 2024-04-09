package com.jcca.web2.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jcca.web2.entity.AssetMode;
import org.apache.ibatis.annotations.Select;

/**
 * @description: 资产类型
 * @author: sophia
 * @create: 2023/11/01 10:20
 **/
public interface AssetModeMapper extends BaseMapper<AssetMode> {

    @Select("select * from asset_mode where code = #{code}")
    AssetMode getByCode(Integer code);
}