package com.jcca.web2.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jcca.web2.entity.CollectDbLockInfo;

import java.util.List;


public interface CollectDbLockInfoService extends IService<CollectDbLockInfo> {


    void updateCollectData(List<CollectDbLockInfo> collectDbLockList, String assetId);
}
