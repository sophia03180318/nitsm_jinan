package com.jcca.web.collect.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jcca.web.collect.entity.CollectSensor;

import java.util.List;

/**
 * 传感采集信息
 *
 * @author Lvyp
 */
public interface CollectSensorService extends IService<CollectSensor> {

    /**
     * 实时缓存
     *
     * @param sensors
     */
    void updateRealTimeData(List<CollectSensor> sensors);

    /**
     * 查询数据
     *
     * @param assetId
     * @param type    传感类型
     * @return
     */
    List<CollectSensor> getRealTimeData(String assetId);

    /**
     * 批量更新
     *
     * @param pcbList assetId必须相同
     */
    void updateBatchByAssetId(List<CollectSensor> entityList);

    /**
     * 清除
     * @param removeHour
     */
    Boolean removeBeforeData(Integer removeHour);
}
