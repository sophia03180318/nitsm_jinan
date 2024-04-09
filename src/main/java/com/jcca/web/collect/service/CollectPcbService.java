package com.jcca.web.collect.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jcca.web.collect.entity.CollectPcb;

import java.util.List;

/**
 * 板卡采集信息
 *
 * @author Lvyp
 */
public interface CollectPcbService extends IService<CollectPcb> {

    /**
     * 更新实时数据缓存
     *
     * @param pcbList
     */
    void updateRealTimeData(List<CollectPcb> pcbList);

    /**
     * 获取实时数据
     *
     * @param assetId
     * @return
     */
    List<CollectPcb> getRealTimeData(String assetId);

    /**
     * 批量更新
     *
     * @param pcbList assetId必须相同
     */
    void updateBatchByAssetId(List<CollectPcb> pcbList);

    /**
     * 删除最新一条数据往前两小时的数据
     *
     * @param removeHour
     * @return
     */
    Boolean removeBeforeData(Integer removeHour);

    /**
     * 获取板卡信息
     */
    List<CollectPcb> getPcdInfoByAsset(String assetId);
}
