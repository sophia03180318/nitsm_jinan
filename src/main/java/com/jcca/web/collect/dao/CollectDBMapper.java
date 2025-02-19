package com.jcca.web.collect.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jcca.web.collect.entity.CollectDB;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 数据库采集的信息
 *
 * @author Lvyp
 */
@Mapper
public interface CollectDBMapper extends BaseMapper<CollectDB> {

    /**
     * 查询资产当前对应最新的数据
     *
     * @param assetId
     * @return
     */
    @Select("SELECT * FROM COLLECT_DB b WHERE ASSET_ID=#{assetId} and b.COLLECT_CODE=(SELECT MAX(to_number(COLLECT_CODE)) AS code FROM COLLECT_DB WHERE ASSET_ID=#{assetId})")
    List<CollectDB> selectRealTimeData(@Param("assetId") String assetId);


}
