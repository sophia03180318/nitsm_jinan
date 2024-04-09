package com.jcca.web.statistics.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcca.web.asset.vo.AssetHistoryVo;
import com.jcca.web.asset.vo.AvgVo;
import com.jcca.web.statistics.dao.HourCpuMapper;
import com.jcca.web.statistics.entity.HourCpu;
import com.jcca.web.statistics.service.HourCpuService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * 一小时统计CPU
 *
 * @author Lvyp
 */
@Service
public class HourCpuServiceImpl extends ServiceImpl<HourCpuMapper, HourCpu> implements HourCpuService {

    @Resource
    private HourCpuMapper hourCpuMapper;

    @Override
    public Date lastCreateDate() {
        return hourCpuMapper.maxCreateDate();
    }

    /**
     * 按时间区间查询资产性能折线数据
     *
     * @param assetId
     * @param startDate
     * @param endDate
     * @return
     */
    @Override
    public List<AssetHistoryVo> findLineByDate(String assetId, Date startDate, Date endDate) {
        return hourCpuMapper.findLineByDate(assetId, startDate, endDate);
    }

    /**
     * 按时间区间统计平均使用率
     *
     * @param assetId
     * @param startDate
     * @param endDate
     * @return
     */
    @Override
    public AvgVo findAvgByDate(String assetId, Date startDate, Date endDate) {
        return hourCpuMapper.findAvgByDate(assetId, startDate, endDate);
    }


}
