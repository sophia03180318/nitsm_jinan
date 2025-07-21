package com.jcca.web.asset.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcca.web.asset.dao.CollectCpuLoadMapper;
import com.jcca.web.asset.entity.CollectCpuLoad;
import com.jcca.web.asset.service.CollectCpuLoadService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * @author: hhw
 * @description: CollectCpuLoadServiceImpl 主要是用来定时清理CPU负载数据
 * @date: 2025-07-03  16:52
 * @since: 2.0.15.0
 */
@Service
public class CollectCpuLoadServiceImpl extends ServiceImpl<CollectCpuLoadMapper, CollectCpuLoad> implements CollectCpuLoadService {
    @Resource
    private CollectCpuLoadMapper collectCpuLoadMapper;

    @Override
    public Boolean removeBeforeData(Integer hour) {
        if (hour < 0) {
            return false;
        }

        Calendar calendar = Calendar.getInstance();
        // 修改为保留资产最近两个小时的数据,而不是删除当前时间两小时前的数据 20220812 hanwone +修改为创建时间
        QueryWrapper<CollectCpuLoad> query = Wrappers.query();
        query.select("asset_id", "max(create_time) as createTime");
        query.groupBy("asset_id");
        List<CollectCpuLoad> list = this.list(query);
        for (CollectCpuLoad collectCpuLoad : list) {
            Date collectTime = collectCpuLoad.getCreateTime();
            if (Objects.nonNull(collectTime)) {
                calendar.setTime(collectTime);
                calendar.add(Calendar.HOUR_OF_DAY, -hour);
                Date time = calendar.getTime();
                query = Wrappers.query();
                query.eq("asset_id", collectCpuLoad.getAssetId());
                query.lt("create_time", time);
                this.remove(query);
            }
        }
        return true;
    }

    @Override
    public CollectCpuLoad getLastRecordByAssetId(String assetId) {
        return collectCpuLoadMapper.getLastRecordByAssetId(assetId);
    }
}
