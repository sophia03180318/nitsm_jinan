package com.jcca.web2.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jcca.web2.entity.CollectBhmTempInfo;

import java.util.List;


public interface CollectBhmTempInfoService extends IService<CollectBhmTempInfo> {

    void updateAssetPcieInfoBatch(List<CollectBhmTempInfo> saveList);
}
