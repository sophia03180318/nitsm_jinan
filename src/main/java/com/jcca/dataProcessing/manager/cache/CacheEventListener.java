package com.jcca.dataProcessing.manager.cache;

import com.jcca.dataProcessing.support.IListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @description: 缓存事件监听
 * @author: Lvyp
 * @create: 2024/01/23 10:30
 */
@Component("cacheEventListener")
public class CacheEventListener implements IListener<CacheEvent> {
    @Resource
    private JccaCacheManager jccaCacheManager;

    @Override
    public void onEvent(CacheEvent event) {

        if(CacheOptEnum.REMOVE==event.getOpt()){
            jccaCacheManager.removeCache(event);
        }
    }
}
