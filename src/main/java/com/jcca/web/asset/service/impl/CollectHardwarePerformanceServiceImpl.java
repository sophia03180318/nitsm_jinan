package com.jcca.web.asset.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcca.web.asset.dao.CollectHardwarePerformanceMapper;
import com.jcca.web.asset.entity.CollectHardwarePerformance;
import com.jcca.web.asset.service.CollectHardwarePerformanceService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author: hhw
 * @description: CollectHardwarePerformanceServiceImpl 主要是用来
 * @date: 2025-09-08  15:16
 * @since: 2.1.9.0
 */
@Service
public class CollectHardwarePerformanceServiceImpl extends ServiceImpl<CollectHardwarePerformanceMapper, CollectHardwarePerformance> implements CollectHardwarePerformanceService {

    @Resource
    private CollectHardwarePerformanceMapper collectHardwarePerformanceMapper;

    /**
     * 根据资产id查询硬件性能信息
     *
     * @param assetId            资产id
     * @param thresholdProcessId
     * @return 硬件性能信息
     */
    @Override
    public List<CollectHardwarePerformance> findByAssetId(String assetId, String thresholdProcessId) {
        return collectHardwarePerformanceMapper.findByAssetId(assetId, thresholdProcessId);
    }
}
