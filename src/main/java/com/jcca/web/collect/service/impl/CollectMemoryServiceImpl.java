package com.jcca.web.collect.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcca.common.enums.UnitEnum;
import com.jcca.common.redis.service.RedisService;
import com.jcca.common.utils.AppMathUtil;
import com.jcca.common.utils.EntityBeanUtil;
import com.jcca.web.collect.dao.CollectMemoryMapper;
import com.jcca.web.collect.entity.CollectMemory;
import com.jcca.web.collect.service.CollectMemoryService;
import com.jcca.web.collect.service.bean.AssetMemoryVo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

/**
 * 运行内存采集
 *
 * @author Lvyp
 */
@Service
public class CollectMemoryServiceImpl extends ServiceImpl<CollectMemoryMapper, CollectMemory>
        implements CollectMemoryService {

    private static final String CACHE_KEY = "MEM:TAB:KEY:";
    private static final String MEM_ALARM = "MEM:ALARM:KEY:";

    @Resource
    private RedisService redisService;
    @Resource
    private CollectMemoryMapper memoryMapper;

    @Override
    public void updateRealTimeData(List<CollectMemory> mems) {
        CollectMemory mem = mems.get(0);
        String key = CACHE_KEY + mem.getAssetId();
        redisService.set(key, mems);
    }

    @Override
    public String getAlarmCode(CollectMemory mem) {
        return MEM_ALARM + mem.getAssetId();
    }

    @Override
    public List<CollectMemory> getRealTimeData(String assetId) {
        List<CollectMemory> memoryList = memoryMapper.selectAssetNewMemory(assetId);
        if (Objects.isNull(memoryList)) {
            return new ArrayList<CollectMemory>();
        }
        return memoryList;
    }

    @Override
    public AssetMemoryVo getAssetMemoryMsg(String assetId, UnitEnum unit) {
        List<CollectMemory> realTimeData = this.getRealTimeData(assetId);
        if (realTimeData.isEmpty()) {
            return null;
        }
        CollectMemory memory = realTimeData.get(0);
        Long scale = unit.getScale();
        AssetMemoryVo vo = EntityBeanUtil.copy(memory, AssetMemoryVo.class);
        if (UnitEnum.AUTO != unit) {
            vo.setMemTotalStr(AppMathUtil.div(memory.getMemTotal(), scale, 2) + unit.name());
            vo.setMemUsedStr(AppMathUtil.div(memory.getMemUsed(), scale, 2) + unit.name());
            vo.setSwapTotalStr(AppMathUtil.div(memory.getSwapTotal(), scale, 2) + unit.name());
            vo.setSwapUsedStr(AppMathUtil.div(memory.getSwapTotal(), scale, 2) + unit.name());
        } else {
            vo.setMemTotalStr(UnitEnum.AutoScale(memory.getMemTotal()));
            vo.setMemUsedStr(UnitEnum.AutoScale(memory.getMemUsed()));
            vo.setSwapTotalStr(UnitEnum.AutoScale(memory.getSwapTotal()));
            vo.setSwapUsedStr(UnitEnum.AutoScale(memory.getSwapUsed()));
        }

        if (StrUtil.isEmpty(vo.getSwapUsedStr())) {
            vo.setSwapUsedStr("0");
        }

        return vo;
    }

    @Override
    public Boolean removeBeforeData(Integer hour) {
        Calendar calendar = Calendar.getInstance();
        // 修改为保留资产最近两个小时的数据,而不是删除当前时间两小时前的数据 20220812 hanwone
        QueryWrapper<CollectMemory> query = Wrappers.query();
        query.select("asset_id", "max(collect_time) as collectTime");
        query.groupBy("asset_id");
        List<CollectMemory> list = this.list(query);
        for (CollectMemory collect : list) {
            Date collectTime = collect.getCollectTime();
            if (Objects.nonNull(collectTime)) {
                calendar.setTime(collectTime);
                calendar.add(Calendar.HOUR_OF_DAY, -hour);
                Date time = calendar.getTime();
                query = Wrappers.query();
                query.eq("asset_id", collect.getAssetId());
                query.lt("collect_time", time);
                this.remove(query);
            }
        }
        return true;
    }

    @Override
    public List<CollectMemory> statistics(Date startDate, Date endDate) {
        return memoryMapper.statisticsGroupByAssetId(startDate, endDate);
    }

}
