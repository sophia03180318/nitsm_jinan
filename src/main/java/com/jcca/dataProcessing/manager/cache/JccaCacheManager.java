package com.jcca.dataProcessing.manager.cache;

/**
 * @description: 缓存管理
 * @author: Lvyp
 * @create: 2024/01/23 10:34
 */
public interface JccaCacheManager {

    /**
     * 删除缓存
     */
    public void removeCache(CacheEvent event);


}
