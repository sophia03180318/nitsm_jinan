package com.jcca.web.collect.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jcca.web.collect.entity.CollectVlan;

import java.util.List;

/**
 * vlan 数据采集
 *
 * @author Lvyp
 */
public interface CollectVlanService extends IService<CollectVlan> {

    /**
     * 实时数据刷新
     *
     * @param vlans
     */
    void updateRealTimeData(List<CollectVlan> vlans);

    /**
     * 获取实时数据
     *
     * @param assetId
     * @return
     */
    List<CollectVlan> getRealTimeData(String assetId);

    /**
     * 批量更新
     *
     * @param vlans 资产id必须相同
     */
    void updateBatchByAssetId(List<CollectVlan> vlans);

}
