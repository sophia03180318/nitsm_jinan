package com.jcca.web2.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.jcca.web2.entity.CollectDatabasesInfo;

import java.util.List;

/**
 * 采集数据库信息
 */
public interface CollectDatabasesInfoService  extends IService<CollectDatabasesInfo> {

    /**
     * 更新采集数据
     * @param collectDatabasesInfos
     * @param dbId
     */
    void updateCollectData(List<CollectDatabasesInfo> collectDatabasesInfos, String assetId);

}
