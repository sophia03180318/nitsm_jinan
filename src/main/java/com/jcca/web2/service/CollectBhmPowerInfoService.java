package com.jcca.web2.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jcca.web2.entity.CollectBhmPowerInfo;

import java.util.List;

/**
 * 电源
 */
public interface CollectBhmPowerInfoService extends IService<CollectBhmPowerInfo> {


    void updateAssetPcieInfoBatch(List<CollectBhmPowerInfo> saveList);

    void updateAssetPcieInfo(CollectBhmPowerInfo copy);
}
