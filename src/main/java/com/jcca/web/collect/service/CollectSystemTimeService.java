package com.jcca.web.collect.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jcca.web.collect.entity.CollectSystemTime;
import com.jcca.web.common.controller.bean.DurationResp;

import java.util.List;

/**
 * 采集系统时间
 *
 * @author Lvyp
 */
public interface CollectSystemTimeService extends IService<CollectSystemTime> {

    /**
     * 更新实时缓存
     *
     * @param sysTimeList
     */
    void updateRealTimeData(List<CollectSystemTime> sysTimeList);

    /**
     * 查询数据
     *
     * @param assetId
     * @return
     */
    List<CollectSystemTime> getRealTimeData(String assetId);

    /**
     * 获取告警的标识
     *
     * @param time
     * @return
     */
    String getAlarmCode(CollectSystemTime time);

    /**
     * 更新
     *
     * @param sysTimeList
     */
    void updateBatchByAssetId(List<CollectSystemTime> sysTimeList);

    /**
     * 删除时间之前的
     *
     * @param removeHour
     * @return
     */
    Boolean removeBeforeData(Integer removeHour);

    /**
     * 根据ASSET_ID获取运行时长
     */
    Long getRunTime(String assetId);

    /**
     * 查询运行时长
     *
     * @return
     */
    List<DurationResp> listGroupByAsset();

    /**
     * 分页查询运行时长
     *
     * @param pageSize
     * @param pageIndex
     * @return
     */
    List<DurationResp> pageGroupByAsset(Integer pageSize, Integer pageIndex);
}
