package com.jcca.web.collect.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jcca.component.thresholds.bean.CollectProcessBean;
import com.jcca.web.collect.entity.CollectProcess;

import java.util.List;

/**
 * 采集进程
 *
 * @author Lvyp
 */
public interface CollectProcessService extends IService<CollectProcess> {

    /**
     * 更新实时数据
     *
     * @param datas
     */
    void updateRealTimeData(List<CollectProcess> datas);

    /**
     * 获取实时数据
     * 获取资产上的进程
     *
     * @param assetId
     * @return
     */
    List<CollectProcessBean> getRealTimeData(String assetId);

    /**
     * 获取告警匹配码
     *
     * @param process
     * @return
     */
    String getAlarmCode(CollectProcess process, String type);

    /**
     * 删除采集到的进程数据
     *
     * @param assetId
     */
    void removeByAssetId(String assetId);

    /**
     * 删除采集到的进程数据
     *
     * @param assetId
     * @param processName
     */
    void removeByAssetIdAndProcessName(String assetId, String processName);

    /**
     * 删除最新一条数据往前两小时的数据
     *
     * @param removeHour
     */
    Boolean removeBeforeData(Integer removeHour);

}
