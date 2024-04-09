package com.jcca.dataProcessing.manager.impl;

import cn.hutool.core.util.StrUtil;
import com.jcca.common.redis.service.RedisService;
import com.jcca.dataProcessing.manager.cache.CacheEvent;
import com.jcca.dataProcessing.manager.cache.JccaCacheManager;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @description: 缓存实现
 * @author: Lvyp
 * @create: 2024/01/23 10:42
 */
@Service
public class JccaCacheManagerService implements JccaCacheManager {

    @Resource
    private RedisService  redisService;

    @Override
    public void removeCache(CacheEvent event) {
        if(StrUtil.isEmpty(event.getEventKey())||StrUtil.isEmpty(event.getMapKey())){
            return ;
        }
        redisService.hmDel(event.getEventKey(),event.getMapKey());
    }
}
