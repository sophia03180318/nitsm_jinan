package com.jcca.web.asset.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jcca.web.asset.entity.AssetHidConf;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AssetHidConfMapper extends BaseMapper<AssetHidConf> {


    @Select("select flag from asset_hid_conf where asset_id=#{assetId} and type=#{type} ")
    List<AssetHidConf> getFlagListByAsset(@Param("assetId") String assetId,@Param("type") String type);
}
