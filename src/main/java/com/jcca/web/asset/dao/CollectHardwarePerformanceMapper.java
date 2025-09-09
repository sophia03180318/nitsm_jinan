package com.jcca.web.asset.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jcca.web.asset.entity.CollectHardwarePerformance;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author: hhw
 * @description: CollectHardwarePerformanceMapper 主要是用来
 * @date: 2025-09-08  15:17
 * @since: 2.1.9.0
 */
public interface CollectHardwarePerformanceMapper extends BaseMapper<CollectHardwarePerformance> {

    @Select("<script>" +
            "SELECT * FROM COLLECT_HARDWARE_PERFORMANCE WHERE ASSET_ID = #{assetId} " +
            "<if test = 'thresholdProcessId != null and thresholdProcessId != \"\"'> " +
            "AND THRESHOLD_PROCESS_ID = #{thresholdProcessId} " +
            "</if>" +
            "</script>")
    List<CollectHardwarePerformance> findByAssetId(@Param("assetId") String assetId, @Param("thresholdProcessId") String thresholdProcessId);
}
