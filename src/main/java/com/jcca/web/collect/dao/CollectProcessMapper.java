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
    List<CollectProcess> selectAssetNewProcess(@Param("assetId") String assetId);
}
