package com.jcca.web2.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.jcca.web2.entity.CollectBhmPcieInfo;

import java.util.List;

/**
 * pcie 信息
 */
public interface CollectBhmPcieInfoService extends IService<CollectBhmPcieInfo> {


    void updateAssetPcieInfoBatch(List<CollectBhmPcieInfo> saveList);
}
