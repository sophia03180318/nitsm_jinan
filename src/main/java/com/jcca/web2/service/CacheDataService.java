package com.jcca.web2.service;

import com.jcca.web.asset.entity.Asset;
import com.jcca.web.collect.enums.InterfaceStatus;
import com.jcca.web2.vo.AssetStatusDetailVo;
import com.jcca.web2.vo.AssetStatusItmVo;

import java.util.List;
import java.util.Map;

/**
 * @description: 缓存数据获取处理
 * @author: Lvyp
 * @create: 2023/12/21 13:51
 */
public interface CacheDataService {


    /**
     * 通过设备ID获取设备当前采集项的状态
     *
     * @param assetId
     * @return
     */
    List<AssetStatusItmVo> queryAssetTargetStatus(String assetId);


    /**
     * 查询设备性能数据V2
     *
     * @param asset
     * @param key
     * @return
     */
    Object queryAssetPerformanceData(Asset asset, String key);

    /**
     * 查询
     * @param assetId
     * @param code
     * @return
     */
    AssetStatusDetailVo getAssetStatusDetailV2(String assetId, String code);

    /**
     * 查询进程TOPO5
     *
     * @param asset
     * @param type  cpuTop5  memTop5
     * @return
     */
    Map<String, Object> getProcessTop5V2(Asset asset, String type);

    /***
     * 查询端口状态是否正常
     * @param assetId 资产ID
     * @param portFullName 端口全称
     * @return
     */
    InterfaceStatus queryInterfaceStatus(String assetId,String portFullName);

}
