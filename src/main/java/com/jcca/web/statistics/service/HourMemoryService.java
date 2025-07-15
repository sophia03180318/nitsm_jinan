package com.jcca.web.statistics.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jcca.web.ai.vo.MemoryVo;
import com.jcca.web.ai.vo.SwapVo;
import com.jcca.web.asset.vo.AssetHistoryVo;
import com.jcca.web.statistics.entity.HourMemory;

import java.util.Date;
import java.util.List;

/**
 * 内存一小时统计
 *
 * @author Lvyp
 */
public interface HourMemoryService extends IService<HourMemory> {

    /**
     * 查询最后一次的生成时间
     *
     * @return
     */
    Date lastCreateDate();

    /**
     * 按时间区间查询资产性能折线数据
     *
     * @param assetId
     * @param startDate
     * @param endDate
     * @return
     */
    List<AssetHistoryVo> findLineByDate(String assetId, Date startDate, Date endDate);

    /**
     * 按天查询内存数据
     */
    List<MemoryVo> findDataByDay(String assetId, Date day);


    /**
     * 按天查询交换空间数据
     */
    List<SwapVo> findSwapByDay(String assetId, Date day);
}
