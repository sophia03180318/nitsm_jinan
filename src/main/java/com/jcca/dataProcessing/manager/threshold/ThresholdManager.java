package com.jcca.dataProcessing.manager.threshold;

import com.jcca.dataProcessing.Entity.ThresholdBaseEntity;

/**
 * 阈值事件管理程序
 *
 * @author Zhaozheng
 * @description TODO
 * @className thresholdManager
 * @date 2023/11/16 16:48
 * @since 2.1.0.0
 */
public interface ThresholdManager {

    /**
     * 获取阈值配置
     * 没有有模块的flag 传空
     * code_assetId_flag
     * @param code
     * @param assetId
     * @param flag
     * @return
     */
    public ThresholdBaseEntity getThresholdValue(String code,String assetId,String flag);

    /**
     * 生成阈值类的阈值设定rediskey
     * @param assetId
     * @param assetIp
     * @return
     */
    public String getThresholdRedisKey(String assetId,String assetIp);

    /**
     * 获取阈值缓存的mapKey
     * @param eventCode
     * @param valueTypeCode
     * @param flag
     * @return
     */
    public String getThresholdMapKey(String eventCode,String valueTypeCode,String flag);

    public void changeThreshold();

}
