package com.jcca.web2.service;

import com.baomidou.mybatisplus.extension.service.IService;

import com.jcca.web2.entity.CollectBhmFanInfo;

import java.util.List;

/**
 * 管理口风扇
 */
public interface CollectBhmFanInfoService extends IService<CollectBhmFanInfo> {

    /**
     * 保存
     * @param saveList
     */
    void updateAssetFanInfoBatch(List<CollectBhmFanInfo> saveList);
}
