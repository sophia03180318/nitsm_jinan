package com.jcca.web.collect.service.impl;

import cn.hutool.core.lang.Assert;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcca.common.redis.service.RedisService;
import com.jcca.web.collect.dao.CollectVlanMapper;
import com.jcca.web.collect.entity.CollectVlan;
import com.jcca.web.collect.service.CollectVlanService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * vlan 数据采集
 *
 * @author Lvyp
 */
@Service
public class CollectVlanServiceImpl extends ServiceImpl<CollectVlanMapper, CollectVlan> implements CollectVlanService {

    private static final String VLAN_KEY = "VLAN:TAB:KEY:";

    @Resource
    private RedisService redisService;
    @Resource
    private CollectVlanMapper vlanMapper;

    @Override
    public void updateRealTimeData(List<CollectVlan> vlans) {
        CollectVlan vlan = vlans.get(0);
        String key = VLAN_KEY + vlan.getAssetId();
        redisService.set(key, JSONUtil.parseArray(vlans));
    }

    @Transactional
    @Override
    public void updateBatchByAssetId(List<CollectVlan> vlans) {
        Assert.isTrue(!vlans.isEmpty(), "updateBatchByAssetId must be not null");
        QueryWrapper<CollectVlan> wrapper = new QueryWrapper<CollectVlan>();
        wrapper.eq("ASSET_ID", vlans.get(0).getAssetId());
        vlanMapper.delete(wrapper);
        saveBatch(vlans);
    }

    @Override
    public List<CollectVlan> getRealTimeData(String assetId) {
        List<CollectVlan> realTimeData = vlanMapper.selectRealTimeData(assetId);
        return realTimeData;
    }

}
