package com.jcca.web.collect.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jcca.common.enums.UnitEnum;
import com.jcca.web.collect.entity.CollectMemory;
import com.jcca.web.collect.service.bean.AssetMemoryVo;

import java.util.Date;
import java.util.List;

/**
 * 采集内存数据
 *
 * @author Lvyp
 */
public interface CollectMemoryService extends IService<CollectMemory> {

    /**
     * 实时数据刷新
     *
     * @param mems
     */
    void updateRealTimeData(List<CollectMemory> mems);

    /**
     * 获取实时数据
     *
     * @param assetId
     * @return
     */
    List<CollectMemory> getRealTimeData(String assetId);

    /**
     * 获取资产对应的内存信息
     *
     * @param assetId
     * @param unit
     * @return
     */
    AssetMemoryVo getAssetMemoryMsg(String assetId, UnitEnum unit);

    /**
     * 获取告警的标识
     *
     * @param mems
     * @return
     */
    String getAlarmCode(CollectMemory mems);

    /**
     * 删除最新一条数据往前两小时的数据
     *
     * @param hour
     * @return
     */
    Boolean removeBeforeData(Integer hour);

    /**
     * 汇总
     *
     * @param lastCreateDate
     * @param now
     * @return
     */
    List<CollectMemory> statistics(Date lastCreateDate, Date now);

}
