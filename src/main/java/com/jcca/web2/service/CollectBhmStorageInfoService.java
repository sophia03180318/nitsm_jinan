package com.jcca.web2.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jcca.dataProcessing.Entity.CollectBhmStorageEntity;
import com.jcca.web2.entity.CollectBhmStorageInfo;

import java.util.List;


public interface CollectBhmStorageInfoService extends IService<CollectBhmStorageInfo> {


    void updateAssetStorageInfoBatch(List<CollectBhmStorageEntity> infoList);

    void updateAssetStorageInfo(CollectBhmStorageEntity info);
}
