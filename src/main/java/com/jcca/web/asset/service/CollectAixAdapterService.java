package com.jcca.web.asset.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jcca.web.asset.entity.CollectAIXAdapter;

import java.util.List;

/**
 * @author GodWone
 * @description 小型机IO卡采集
 * @className CollectAixAdapterService
 * @date 2023/2/7 10:31
 * @since 2.0.0.1
 */
public interface CollectAixAdapterService extends IService<CollectAIXAdapter> {

    /**
     * IO卡最新信息
     *
     * @param assetId 资产ID
     * @return 信息集合
     */
    List<CollectAIXAdapter> latestInfo(String assetId);
}
