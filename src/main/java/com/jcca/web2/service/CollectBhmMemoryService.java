package com.jcca.web2.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jcca.web2.entity.CollectBhmMemoryInfo;

import java.util.List;


/**
 * 采集管理口内存
 */
public interface CollectBhmMemoryService extends IService<CollectBhmMemoryInfo> {

    /**
     * 保存管理口的信息
     * @param infoList
     */
    void updateAssetFanInfoBatch(List<CollectBhmMemoryInfo> infoList);

    void updateAssetFanInfo(CollectBhmMemoryInfo copy);
}
