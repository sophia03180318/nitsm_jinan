package com.jcca.web2.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jcca.web2.entity.CollectBhmCpuInfo;

import java.util.List;


/**
 * 管理口CPU信息
 */
public interface CollectBhmCpuInfoService extends IService<CollectBhmCpuInfo> {

    /**
     * 保存新的cpu数据删除老的
     * @param infoList
     */
    void updateAssetCpuInfoBatch(List<CollectBhmCpuInfo> infoList);
}
