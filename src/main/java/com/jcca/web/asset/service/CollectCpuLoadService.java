package com.jcca.web.asset.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jcca.web.asset.entity.CollectCpuLoad;

/**
 * @author: hhw
 * @description: CollectCpuLoadService 主要是用来
 * @date: 2025-07-03  16:51
 * @since: 2.0.15.0
 */
public interface CollectCpuLoadService extends IService<CollectCpuLoad> {
    Boolean removeBeforeData(Integer removeHour);

    CollectCpuLoad getLastRecordByAssetId(String assetId);
}
