package com.jcca.web.asset.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jcca.web.asset.entity.CollectHardware;

/**
 * @author: hhw
 * @description: CollectHardwareService 主要是用来
 * @date: 2025-08-25  15:11
 * @since: 2.1.9.0
 */
public interface CollectHardwareService extends IService<CollectHardware> {

    CollectHardware findByAssetId(String assetId);
}
