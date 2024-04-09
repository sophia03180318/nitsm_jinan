package com.jcca.dataProcessing.manager.cache;

import lombok.Data;

/**
 * @description: 触发缓存操作的event
 * @author: Lvyp
 * @create: 2024/01/23 10:22
 */
@Data
public class CacheEvent {

    /**
     * 资产ID
     */
    private String assetId;
    /**
     * 资产IP
     */
    private String assetIp;
    /**
     * 事件KEY
     */
    private String eventKey;
    /**
     * mapKey
     */
    private String mapKey;

    private CacheOptEnum opt;
}
