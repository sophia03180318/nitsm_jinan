package com.jcca.web.asset.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jcca.web.asset.entity.CollectAIXAdapter;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CollectAIXAdapterMapper extends BaseMapper<CollectAIXAdapter> {

    @Select("SELECT * FROM COLLECT_AIX_ADAPTER WHERE COLLECT_CODE = (SELECT MAX(COLLECT_CODE) AS CODE FROM COLLECT_AIX_ADAPTER WHERE ASSET_ID = #{assetId})")
    List<CollectAIXAdapter> selectLatestInfo(String assetId);
}