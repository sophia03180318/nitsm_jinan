package com.jcca.web.asset.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcca.web.asset.dao.CollectHardwareMapper;
import com.jcca.web.asset.entity.CollectHardware;
import com.jcca.web.asset.service.CollectHardwareService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author: hhw
 * @description: CollectHardwareImpl 主要是用来
 * @date: 2025-08-25  15:12
 * @since: 2.1.9.0
 */
@Service
public class CollectHardwareImpl extends ServiceImpl<CollectHardwareMapper, CollectHardware> implements CollectHardwareService {

    @Resource
    private CollectHardwareMapper collectHardwareMapper;

    @Override
    public CollectHardware findByAssetId(String assetId) {
        return collectHardwareMapper.findByAssetId(assetId);
    }
}
