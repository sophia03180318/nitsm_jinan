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
    List<CollectMemory> selectAssetNewMemory(@Param("assetId") String assetId);

    /**
     * 根据assetId进行分组汇总
     *
     * @param startDate
     * @param endDate
     * @return
     */
    List<CollectMemory> statisticsGroupByAssetId(@Param("startDate") Date startDate, @Param("endDate") Date endDate);

}
