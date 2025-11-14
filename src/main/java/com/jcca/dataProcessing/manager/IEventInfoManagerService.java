package com.jcca.dataProcessing.manager;

import com.jcca.dataProcessing.Entity.ChangeInfo;
import com.jcca.dataProcessing.manager.bean.AlarmTempReq;
import com.jcca.dataProcessing.support.IEvent;

/**
 * @author Zhaozheng
 * @description TODO
 * @className IEventInfoChangeManagerService
 * @date 2023/10/20 9:35
 * @since 2.1.0.0
 */
public interface IEventInfoManagerService {


    /**
     * 信息是否有变动  true为有变化(此方法可返回两种状态：有变动、无变动)
     *
     * @param redisKey
     * @param mapKey
     * @param changeValue
     * @return
     */
    public boolean infoIschange(String inspectRecordId,String redisKey, String mapKey, Object changeValue);

    /**
     * 判断信息是否有变动(此方法返回3中状态：null代表第一次，有变动，无变动)
     *
     * @param redisKey
     * @param mapKey
     * @param changeValue
     * @return
     */
    public Boolean infoIschangeFirst(String inspectRecordId,String redisKey, String mapKey, Object changeValue);

    /**
     * 通过rediskey的信息，清除缓存
     *
     * @param redisKey
     */
    public void delRedisKey(String redisKey);

    /**
     * 删除历史缓存，并添加新的缓存内容
     *
     * @param redisKey
     * @param mapKey
     * @param value
     */
    public void delAndsetStateValue(String redisKey, String mapKey, Object value);

    /**
     * 覆盖添加缓存信息
     *
     * @param redisKey
     * @param mapKey
     * @param value
     */
    public void setStateValue(String redisKey, String mapKey, Object value);

    /**
     * 删除缓存信息
     *
     * @param redisKey
     * @param mapKey
     */
    public void delStateValue(String redisKey, String mapKey);

    /**
     * 直接创建时间信息
     *
     * @param assetId
     * @param changeInfo
     * @param redisKey
     * @param mapKey
     * @param status
     * @return
     */
    public IEvent creatChangeEvent(String assetId, ChangeInfo changeInfo, String redisKey, String mapKey, Integer status, AlarmTempReq alarmTempReq,String inspectRecordId,String version);

    /**
     * 创建恢复事件
     *
     * @param assetId
     * @param changeInfo
     * @param eventRedisKey
     * @param EventMapKey
     * @param redisThresholdKey
     * @param thresholdMapKey
     * @return
     */
    public IEvent creatRecoveryThresholdEvent(String assetId, ChangeInfo changeInfo, String eventRedisKey, String EventMapKey, String redisThresholdKey, String thresholdMapKey,String inspectRecordId);

    /**
     * 获取缓存信息
     *
     * @param redisKey
     * @param mapKey
     * @return
     */
    public Object getStateValue(String redisKey, String mapKey);

    /**
     * 判断数据状态
     * @param redisKey
     * @param mapKey
     * @param changeValue
     * @return
     */
    public  Boolean infoChangeStatus(String redisKey, String mapKey, Object changeValue);

}
