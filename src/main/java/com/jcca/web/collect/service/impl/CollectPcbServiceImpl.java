package com.jcca.web.collect.service.impl;

import cn.hutool.core.lang.Assert;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcca.common.redis.service.RedisService;
import com.jcca.web.collect.dao.CollectPcbMapper;
import com.jcca.web.collect.entity.CollectPcb;
import com.jcca.web.collect.service.CollectPcbService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;


/**
 * 板卡采集的数据
 *
 * @author Lvyp
 */
@Service
public class CollectPcbServiceImpl extends ServiceImpl<CollectPcbMapper, CollectPcb> implements CollectPcbService {

    private static final String PCB_KEY = "PCB:TAB:KEY:";

    @Resource
    private RedisService redisService;
    @Resource
    private CollectPcbMapper pcbMapper;

    @Override
    public void updateRealTimeData(List<CollectPcb> pcbList) {
        CollectPcb pcb = pcbList.get(0);
        String key = PCB_KEY + pcb.getAssetId();
        redisService.set(key, JSONUtil.parseArray(pcbList));
    }

    @Override
    public void updateBatchByAssetId(List<CollectPcb> pcbList) {
        Assert.isTrue(!pcbList.isEmpty(), "updateBatchByAssetId must be not null");
        String assetId = pcbList.get(0).getAssetId();
        QueryWrapper<CollectPcb> wrapper = new QueryWrapper<CollectPcb>();
        wrapper.eq("ASSET_ID", assetId);
        pcbMapper.delete(wrapper);
        saveBatch(pcbList);
    }

    @Override
    public List<CollectPcb> getRealTimeData(String assetId) {

        List<CollectPcb> entityList = pcbMapper.selectRealTimeData(assetId);
        return entityList;
    }

    @Override
    public Boolean removeBeforeData(Integer hour) {
        Calendar calendar = Calendar.getInstance();
        // 修改为保留资产最近两个小时的数据,而不是删除当前时间两小时前的数据 20220812 hanwone
        QueryWrapper<CollectPcb> query = Wrappers.query();
        query.select("asset_id", "max(collect_time) as collectTime");
        query.groupBy("asset_id");
        List<CollectPcb> list = this.list(query);
        for (CollectPcb collect : list) {
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
    public List<CollectPcb> getPcdInfoByAsset(String assetId) {
        return pcbMapper.getPcdInfoByAsset(assetId);
    }
}
