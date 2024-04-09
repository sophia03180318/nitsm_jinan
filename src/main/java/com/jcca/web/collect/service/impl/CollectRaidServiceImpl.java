package com.jcca.web.collect.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcca.web.collect.dao.CollectRaidMapper;
import com.jcca.web.collect.entity.CollectRaid;
import com.jcca.web.collect.service.CollectRaidService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * @ Author：sophia
 * @ Date：Created in 0:31 2022/6/9
 * @ Description:
 */
@Service
public class CollectRaidServiceImpl extends ServiceImpl<CollectRaidMapper, CollectRaid> implements CollectRaidService {
    @Resource
    private CollectRaidMapper raidMapper;

    @Override
    public List<CollectRaid> findByType(String assetId, String groupId, int type) {
        List<CollectRaid> list;
        if (ObjectUtil.isNull(groupId)) {
            list = raidMapper.findByType(assetId, type);
        } else {
            list = raidMapper.findByGroup(assetId, groupId, type);
        }
        return list;
    }

    @Override
    public List<CollectRaid> findDrive(String assetId) {
        List<CollectRaid> drive = raidMapper.findDrive(assetId);
        return drive;
    }

    @Override
    public Date findLastTime(String assetId) {
        return raidMapper.findLastTime(assetId);
    }

    @Override
    public Boolean removeBeforeData(Integer hour) {
        Calendar calendar = Calendar.getInstance();
        // 修改为保留资产最近两个小时的数据,而不是删除当前时间两小时前的数据 20220812 hanwone
        QueryWrapper<CollectRaid> query = Wrappers.query();
        query.select("asset_id", "max(collect_time) as collectTime");
        query.groupBy("asset_id");
        List<CollectRaid> list = this.list(query);
        for (CollectRaid collect : list) {
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


}
