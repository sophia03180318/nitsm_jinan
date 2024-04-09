package com.jcca.web.collect.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jcca.web.collect.entity.CollectCpu;

import java.util.Date;
import java.util.List;

/**
 * cpu数据采集
 *
 * @author Lvyp
 */
public interface CollectCpuService extends IService<CollectCpu> {

    /**
     * 缓存采集的实时数据
     *
     * @param cpuList
     */
    void updateRealTimeData(List<CollectCpu> cpuList);

    /**
     * 获取该资产CPU的实时数据
     *
     * @param assetId
     * @return
     */
    List<CollectCpu> getRealTimeData(String assetId);

    /**
     * 删除最新一条数据往前两小时的数据
     *
     * @return
     */
    Boolean removeBeforeData(Integer hour);

    /**
     * 获取唯一的告警编号
     *
     * @param cpu
     * @return
     */
    String getAlarmCode(CollectCpu cpu);

    /**
     * 汇总
     *
     * @param createTime
     * @param now
     */
    List<CollectCpu> statistics(Date createTime, Date now);

    /**
     * 查询最新的一条数据
     */
    CollectCpu selectMaxOne();
}
