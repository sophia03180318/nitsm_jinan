package com.jcca.web.collect.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcca.web.collect.dao.CollectTablespaceMapper;
import com.jcca.web.collect.entity.CollectTablespace;
import com.jcca.web.collect.service.CollectTablespaceService;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * @author hanwone
 * @date 2020-08-06 09:55
 **/
@Service
public class CollectTablespaceServiceImpl extends ServiceImpl<CollectTablespaceMapper, CollectTablespace> implements CollectTablespaceService {

    @Override
    public Boolean removeBeforeData(Integer hour) {
        Calendar calendar = Calendar.getInstance();
        // 修改为保留资产最近两个小时的数据,而不是删除当前时间两小时前的数据 20220812 hanwone
        QueryWrapper<CollectTablespace> query = Wrappers.query();
        query.select("asset_id", "max(collect_time) as collectTime");
        query.groupBy("asset_id");
        List<CollectTablespace> list = this.list(query);
        for (CollectTablespace collect : list) {
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
    public void updateOrSaveByName(CollectTablespace tablespace) {
        String name = tablespace.getName();
        QueryWrapper<CollectTablespace> query = new QueryWrapper<>();
        query.eq("NAME", tablespace.getName());
        query.eq("ASSET_ID", tablespace.getAssetId());
        CollectTablespace one = getOne(query);
        if (Objects.nonNull(one)) {
            tablespace.setCollectDbId(one.getId());
            updateById(tablespace);
            return;
        }

        save(tablespace);
    }
}