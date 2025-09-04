package com.jcca.web2.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jcca.web2.entity.CollectDbProcessLockInfo;

import java.util.List;

/**
 * 进程信息
 */
public interface CollectDbProcessLockInfoService extends IService<CollectDbProcessLockInfo> {


    void updateCollectData(List<CollectDbProcessLockInfo> collectProcessLockList, String dbId);
}
