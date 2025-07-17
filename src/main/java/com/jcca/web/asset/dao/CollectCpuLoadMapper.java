package com.jcca.web.asset.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jcca.web.asset.entity.CollectCpuLoad;
import org.apache.ibatis.annotations.Select;

/**
 * @author: hhw
 * @description: CollectCpuLoadMapper 主要是用来
 * @date: 2025-07-03  16:51
 * @since: 2.0.15.0
 */
public interface CollectCpuLoadMapper extends BaseMapper<CollectCpuLoad> {

    @Select("SELECT * FROM COLLECT_CPU_LOAD WHERE ID = (SELECT MAX(ID) FROM COLLECT_CPU_LOAD WHERE ASSET_ID = #{assetId})")
    CollectCpuLoad getLastRecordByAssetId(String assetId);
}
