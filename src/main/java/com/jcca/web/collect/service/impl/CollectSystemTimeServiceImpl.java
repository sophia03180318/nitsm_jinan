package com.jcca.web.collect.service.impl;

import cn.hutool.core.lang.Assert;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcca.common.redis.service.RedisService;
import com.jcca.web.collect.dao.CollectSystemTimeMapper;
import com.jcca.web.collect.entity.CollectSystemTime;
import com.jcca.web.collect.service.CollectSystemTimeService;
import com.jcca.web.common.controller.bean.DurationResp;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * 系统时间采集
 *
 * @author Lvyp
 */
@Service
public class CollectSystemTimeServiceImpl extends ServiceImpl<CollectSystemTimeMapper, CollectSystemTime>
        implements CollectSystemTimeService {

    private static final String TIME_KEY = "SYSTEM:TIME:TAB:KEY:";
    private static final String TIME_ALARM = "SYSTEM:TIME:ALARM:KEY:";

    @Resource
    private RedisService redisService;
    @Resource
    private CollectSystemTimeMapper systemTimeMapper;

    @Override
    public void updateRealTimeData(List<CollectSystemTime> sysTimeList) {
        CollectSystemTime systemTime = sysTimeList.get(0);
        String key = TIME_KEY + systemTime.getAssetId();
        redisService.set(key, JSONUtil.parseArray(sysTimeList));
    }

    @Override
    public List<CollectSystemTime> getRealTimeData(String assetId) {
        List<CollectSystemTime> systemData = systemTimeMapper.selectAssetNewSystem(assetId);
        return systemData;
    }

    @Override
    public String getAlarmCode(CollectSystemTime time) {
        return TIME_ALARM + time.getAssetId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateBatchByAssetId(List<CollectSystemTime> entityList) {
        Assert.isTrue(!entityList.isEmpty(), "updateBatchByAssetId mast be not null");
        String assetId = entityList.get(0).getAssetId();
        if (StringUtils.isEmpty(assetId)) {
            return;
        }
        QueryWrapper<CollectSystemTime> wrapper = new QueryWrapper<CollectSystemTime>();
        wrapper.eq("ASSET_ID", assetId);
        systemTimeMapper.delete(wrapper);
        saveBatch(entityList);
    }

    @Override
    public Boolean removeBeforeData(Integer hour) {
        Calendar calendar = Calendar.getInstance();
        // 修改为保留资产最近两个小时的数据,而不是删除当前时间两小时前的数据 20220812 hanwone
        QueryWrapper<CollectSystemTime> query = Wrappers.query();
        query.select("asset_id", "collect_time");
        query.groupBy("asset_id", "collect_time");
        query.orderByDesc("collect_time");
        List<CollectSystemTime> list = this.list(query);
        for (CollectSystemTime collect : list) {
            Date collectTime = collect.getCollectTime();
            if (Objects.nonNull(collectTime)) {
                calendar.setTime(collectTime);
                calendar.add(Calendar.HOUR, -hour);
                Date time = calendar.getTime();
                query = Wrappers.query();
                query.eq("asset_id", collect.getAssetId());
                query.le("collect_time", time);
                this.remove(query);
            }
        }
        return true;
    }

    @Override
    public Long getRunTime(String assetId) {
        List<Long> runTime = systemTimeMapper.getRunTime(assetId);
        if (runTime.size() > 0) {
            return runTime.get(0);
        }
        return 0L;
    }

    @Override
    public List<DurationResp> listGroupByAsset() {

        return systemTimeMapper.listGroupByAsset();
    }

    @Override
    public List<DurationResp> pageGroupByAsset(Integer pageSize, Integer pageIndex) {

        return systemTimeMapper.pageGroupByAsset(pageSize, pageIndex);
    }

}
