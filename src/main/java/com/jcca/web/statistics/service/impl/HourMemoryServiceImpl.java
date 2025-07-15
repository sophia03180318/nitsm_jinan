package com.jcca.web.statistics.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcca.web.asset.vo.AssetHistoryVo;
import com.jcca.web.statistics.dao.HourMemoryMapper;
import com.jcca.web.statistics.entity.HourMemory;
import com.jcca.web.statistics.service.HourMemoryService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * 一小时统计
 *
 * @author Lvyp
 */
@Service
public class HourMemoryServiceImpl extends ServiceImpl<HourMemoryMapper, HourMemory> implements HourMemoryService {

    @Resource
    private HourMemoryMapper memoryMapper;

    @Override
    public Date lastCreateDate() {
        return memoryMapper.maxCreateDate();
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
        return memoryMapper.findLineByDate(assetId, startDate, endDate);
    }

}
