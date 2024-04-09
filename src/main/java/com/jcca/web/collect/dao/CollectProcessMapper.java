package com.jcca.web.collect.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jcca.web.collect.entity.CollectProcess;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 进程
 *
 * @author Lvyp
 */
@Mapper
public interface CollectProcessMapper extends BaseMapper<CollectProcess> {

    /**
     * 查询资产当前对应最新的进程数据
     *
     * @param assetId
     * @return
     */
    @Select("SELECT * FROM COLLECT_PROCESS b WHERE b.COLLECT_CODE=(SELECT MAX(to_number(COLLECT_CODE)) AS code FROM COLLECT_PROCESS WHERE ASSET_ID=#{assetId})")
    List<CollectProcess> selectAssetNewProcess(@Param("assetId") String assetId);
}
