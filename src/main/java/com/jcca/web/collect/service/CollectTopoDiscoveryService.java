package com.jcca.web.collect.service;

import com.jcca.web.asset.entity.Asset;

import java.util.Map;

public interface CollectTopoDiscoveryService {
    /**
     * 端口所属vlan相关信息
     */
    public Object vlanInfo(Asset asset);

    public Object HuaWeivlanInfo(Asset asset);

    /**
     * 获取端口信息
     */
    public Object interfaceInfo(Asset asset);

    /**
     * arp相关信息
     */
    public Object arpInfo(Asset asset, Map<String, String> assetAllArpInfo);

    /**
     * CDP相关信息
     */
    public Object cdpInfo(Asset asset);

    /**
     * 信息转发连接关系
     */
    public Object bridgeInfo(Asset asset, String vlanName);

    public Object h3CbridgeInfo(Asset asset);

    public Object huaWeibridgeInfo(Asset asset);

    /**
     * 路由信息
     */
    public Object routeInfo(Asset asset);
}
