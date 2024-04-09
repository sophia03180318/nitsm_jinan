package com.jcca.web.collect.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.collect.controller.route.bean.AssetLinkAssetVo;
import com.jcca.web.collect.controller.route.bean.RouteMsg;
import com.jcca.web.collect.entity.CollectRoute;
import com.jcca.web.collect.service.bean.TopoRouteVo;

import java.util.List;

/**
 * 采集路由设备
 *
 * @author lyp
 */
public interface CollectRouteService extends IService<CollectRoute> {

    /**
     * 采集路由
     */
    void collectRoute(Asset asset);

    List<CollectRoute> queryCollectRoute(String assetId);

    /**
     * 通过网卡信息查询匹配
     *
     * @param assetId
     * @param macAddr
     * @return
     */
    RouteMsg queryRouteMsg(String assetId, String macAddr);

    List<TopoRouteVo> queryTopoRoute(String assetId);

    /**
     * 查找对端信息
     *
     * @param asetId
     * @return
     */
    List<AssetLinkAssetVo> findAtAssetAndPort(String asetId);
}
