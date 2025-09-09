package com.jcca.web.asset.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jcca.web.asset.entity.CollectHardwarePerformance;

import java.util.List;

/**
 * @author: hhw
 * @description: CollectHardwarePerformanceService 主要是用来
 * @date: 2025-08-25  15:11
 * @since: 2.1.9.0
 */
public interface CollectHardwarePerformanceService extends IService<CollectHardwarePerformance> {

    /**
     * 根据资产id查询硬件性能信息
     *
     * @param assetId            资产id
     * @param thresholdProcessId
     * @return 硬件性能信息
     */
    List<CollectHardwarePerformance> findByAssetId(String assetId, String thresholdProcessId);
}
