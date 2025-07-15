package com.jcca.web.statistics.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jcca.web.asset.vo.AssetHistoryVo;
import com.jcca.web.asset.vo.AvgVo;
import com.jcca.web.statistics.entity.HourCpu;

import java.util.Date;
import java.util.List;

/**
 * 一小时统计
 *
 * @author Lvyp
 */
public interface HourCpuService extends IService<HourCpu> {

    /**
     * 查找最后一次的创建时间
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
     * 按时间区间统计平均使用率
     *
     * @param assetId
     * @param startDate
     * @param endDate
     * @return
     */
    AvgVo findAvgByDate(String assetId, Date startDate, Date endDate);
}
