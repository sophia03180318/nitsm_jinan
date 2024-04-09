package com.jcca.web.collect.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcca.common.redis.service.RedisService;
import com.jcca.web.collect.dao.CollectConnectMapper;
import com.jcca.web.collect.entity.CollectConnect;
import com.jcca.web.collect.service.CollectConnectService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author LuBan
 * @since 2021-04-27
 */
@Service
public class CollectConnectServiceImpl extends ServiceImpl<CollectConnectMapper, CollectConnect> implements CollectConnectService {
    private static final String CONNECT_KEY = "CONNECT:TAB:KEY:";
    @Resource
    private RedisService redisService;

    @Override
    public void updateRealTimeData(List<CollectConnect> connectList) {
        CollectConnect connect = connectList.get(0);
        String key = CONNECT_KEY + connect.getAssetId();
        redisService.set(key, JSONUtil.parseArray(connectList));
    }

    @Override
    public List<CollectConnect> getRealTimeData(String assetId) {
        QueryWrapper<CollectConnect> qw = new QueryWrapper<>();
        qw.eq("ASSET_ID", assetId);
        List<CollectConnect> connectData = this.list(qw);
        if (CollUtil.isEmpty(connectData)) {
            return new ArrayList<CollectConnect>();
        }
        return connectData;
    }

    @Override
    public String getAlarmCode(List<CollectConnect> connectList) {
        return null;
    }

    @Override
    public void updateBatchByAssetId(List<CollectConnect> entityList) {
        Assert.isTrue(!entityList.isEmpty(), "updateBatchByAssetId mast be not null");
        String assetId = entityList.get(0).getAssetId();
        QueryWrapper<CollectConnect> wrapper = new QueryWrapper<>();
        wrapper.eq("ASSET_ID", assetId);
        this.remove(wrapper);
        saveBatch(entityList);
    }


}
