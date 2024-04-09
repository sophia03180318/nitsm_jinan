package com.jcca.web.collect.service.impl;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcca.common.redis.service.RedisService;
import com.jcca.web.collect.dao.CollectCpuMapper;
import com.jcca.web.collect.entity.CollectCpu;
import com.jcca.web.collect.service.CollectCpuService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * 采集cpu数据
 *
 * @author Lvyp
 */
@Service
public class CollectCpuServiceImpl extends ServiceImpl<CollectCpuMapper, CollectCpu> implements CollectCpuService {

    private static final String CPU_KEY = "CPU:TAB:KEY:";
    private static final String CPU_ALARM = "CPU:ALARM:KEY:";

    @Resource
    private RedisService redisService;
    @Resource
    private CollectCpuMapper collectCpuMapper;

    @Override
    public void updateRealTimeData(List<CollectCpu> cpuList) {
        CollectCpu cpu = cpuList.get(0);

        String key = CPU_KEY + cpu.getAssetId();
        redisService.set(key, JSONUtil.parseArray(cpuList));
    }

    @Override
    public String getAlarmCode(CollectCpu cpu) {
        return CPU_ALARM + cpu.getAssetId() + "CPUFLG" + cpu.getCpuFlg();
    }

    @Override
    public List<CollectCpu> getRealTimeData(String assetId) {
        Assert.isTrue(StrUtil.isNotEmpty(assetId), "资产ID不能空");
        List<CollectCpu> realTimeData = collectCpuMapper.selectRealTimeData(assetId);
        return realTimeData;
    }

    @Override
    public Boolean removeBeforeData(Integer hour) {
        if (hour < 0) {
            return false;
        }

        Calendar calendar = Calendar.getInstance();
        // 修改为保留资产最近两个小时的数据,而不是删除当前时间两小时前的数据 20220812 hanwone
        QueryWrapper<CollectCpu> query = Wrappers.query();
        query.select("asset_id", "max(collect_time) as collectTime");
        query.groupBy("asset_id");
        List<CollectCpu> list = this.list(query);
        for (CollectCpu collectCpu : list) {
            Date collectTime = collectCpu.getCollectTime();
            if (Objects.nonNull(collectTime)) {
                calendar.setTime(collectTime);
                calendar.add(Calendar.HOUR_OF_DAY, -hour);
                Date time = calendar.getTime();
                query = Wrappers.query();
                query.eq("asset_id", collectCpu.getAssetId());
                query.lt("collect_time", time);
                this.remove(query);
            }
        }
        return true;
    }

    @Override
    public List<CollectCpu> statistics(Date startDate, Date endDate) {
        return collectCpuMapper.statisticsByDate(startDate, endDate);
    }

    @Override
    public CollectCpu selectMaxOne() {
        return collectCpuMapper.selectMaxOne();
    }

}
