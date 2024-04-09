package com.jcca.web.collect.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jcca.web.collect.entity.CollectMemory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Date;
import java.util.List;

/**
 * 内存数据采集
 *
 * @author Lvyp
 */
@Mapper
public interface CollectMemoryMapper extends BaseMapper<CollectMemory> {

    /**
     * 查询资产当前对应最新数据
     *
     * @param assetId
     * @return
     */
    @Select("SELECT * FROM COLLECT_MEMORY b WHERE b.COLLECT_CODE=(SELECT MAX(to_number(COLLECT_CODE)) AS code FROM COLLECT_MEMORY WHERE ASSET_ID=#{assetId})")
    List<CollectMemory> selectAssetNewMemory(@Param("assetId") String assetId);

    /**
     * 根据assetId进行分组汇总
     *
     * @param startDate
     * @param endDate
     * @return
     */
    @Select("SELECT MAX(ID) AS ID,MAX(COLLECT_TIME) AS COLLECT_TIME,MAX(to_number(COLLECT_CODE)) AS COLLECT_CODE,ASSET_ID,ROUND(AVG(MEM_TOTAL)) AS MEM_TOTAL,ROUND(AVG(MEM_USED)) AS MEM_USED,ROUND(AVG(MEM_USED_RATE),2) AS MEM_USED_RATE,ROUND(AVG(SWAP_TOTAL)) AS SWAP_TOTAL,ROUND(AVG(SWAP_USED)) AS SWAP_USED,ROUND(AVG(SWAP_USED_RATE),2) AS SWAP_USED_RATE,MAX(CREATE_TIME) FROM COLLECT_MEMORY WHERE COLLECT_TIME BETWEEN #{startDate} AND #{endDate} GROUP BY ASSET_ID")
    List<CollectMemory> statisticsGroupByAssetId(@Param("startDate") Date startDate, @Param("endDate") Date endDate);

}
