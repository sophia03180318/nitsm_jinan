package com.jcca.web.collect.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jcca.web.collect.entity.CollectDB;

import java.util.List;

/**
 * 数据库采集
 *
 * @author Lvyp
 */
public interface CollectDBService extends IService<CollectDB> {

    /**
     * 更新实时数据
     *
     * @param entityList
     */
    void updateRealTimeData(List<CollectDB> entityList);

    /**
     * 获取实时数据
     *
     * @param assetId
     * @return
     */
    List<CollectDB> getRealTimeData(String assetId);

    /**
     * 删除最新一条数据往前两小时的数据
     *
     * @param hour
     * @return
     */
    Boolean removeBeforeData(Integer hour);

}
