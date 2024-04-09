package com.jcca.web.collect.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jcca.web.collect.entity.CollectDS;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Date;
import java.util.List;

/**
 * @ Author：sophia
 * @ Date：Created in 0:32 2022/6/9
 * @ Description:
 */
@Mapper
public interface CollectDsMapper extends BaseMapper<CollectDS> {

    @Select("select a.* from COLLECT_DS a where COLLECT_CODE=(SELECT MAX(to_number(COLLECT_CODE)) AS code  from COLLECT_DS where ASSET_ID=#{assetId} and TYPE=#{type}) and ASSET_ID=#{assetId} and TYPE=#{type}")
    List<CollectDS> findByType(String assetId, int type);

    @Select("SELECT MAX(COLLECT_TIME) AS code  from COLLECT_DS where ASSET_ID=#{assetId}")
    Date findLastTime(String assetId);

    @Select("select a.* from COLLECT_DS a where PARENT_ORG_ID=#{arrayId} and type =2 ")
    List<CollectDS> findByArray(String arrayId);


    @Select("select a.* from COLLECT_DS a where COLLECT_CODE=(SELECT MAX(to_number(COLLECT_CODE)) AS code  from COLLECT_DS where ASSET_ID=#{assetId} and TYPE=#{type}) and ASSET_ID=#{assetId} and TYPE=#{type} and Y_INDEX = #{index}")
    List<CollectDS> findByDrives(String assetId, int index);
}
