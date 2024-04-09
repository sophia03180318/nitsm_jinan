package com.jcca.web.collect.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jcca.common.enums.UnitEnum;
import com.jcca.web.collect.entity.CollectDisk;
import com.jcca.web.collect.service.bean.AssetDiskVo;

import java.util.List;

/**
 * 磁盘采集信息
 *
 * @author Lvyp
 */
public interface CollectDiskService extends IService<CollectDisk> {

    /**
     * 缓存实时数据
     *
     * @param diskList
     */
    void updateRealTimeData(List<CollectDisk> diskList);

    /**
     * 获取资产的所有磁盘信息
     *
     * @param assetId
     * @return
     */
    List<CollectDisk> getRealTimeData(String assetId);

    /**
     * 获取资产对应的磁盘汇总信息
     *
     * @param assetId
     * @param unit
     * @return
     */
    AssetDiskVo getAssetDiskMsg(String assetId, UnitEnum unit);


    /**
     * 获取磁盘告警匹配code
     *
     * @param disk
     * @return
     */
    String getAlarmCode(CollectDisk disk);

    /**
     * 批量更新
     *
     * @param diskList 资产id必须相同。
     */
    void updateBatchByAssetId(List<CollectDisk> diskList);

}
