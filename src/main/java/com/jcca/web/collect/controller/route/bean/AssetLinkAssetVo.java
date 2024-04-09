package com.jcca.web.collect.controller.route.bean;

import lombok.Data;

/**
 * @author hanhw
 * @description 设备连接信息
 * @className AssetLinkAsset
 * @date 2023/4/25 15:57
 * @since 2.0.3.0
 */
@Data
public class AssetLinkAssetVo {
    /**
     * 资产ID
     */
    private String assetId;
    /**
     * 端口
     */
    private String portIndex;
    /**
     * 端口IP
     */
    private String portIp;
    /**
     * 接收流量
     */
    private String portIn;
    /**
     * 发送流量
     */
    private String portOut;
    /**
     * 端口状态
     */
    private String portStatus;
    /**
     * 本端端口索引
     */
    private String portIndexRank;
    /**
     * 拓扑配置的对端设备ID
     */
    private String maAssetId;
    /**
     * 拓扑配置的对端设备名称
     */
    private String maAssetName;
    /**
     * 拓扑配置的对端设备IP
     */
    private String maAssetIp;
    /**
     * 拓扑配置的对端设备端口
     */
    private String maPort;
    /**
     * 自动发现的对端设备ID
     */
    private String atAssetId;
    /**
     * 自动发现的对端设备名称
     */
    private String atName;
    /**
     * 自动发现的对端设备IP
     */
    private String atNetAddress;
    /**
     * 自动发现的对端设备端口
     */
    private String atPortIndexName;
    /**
     * 自动发现的对端端口MAC地址
     */
    private String atPhysAddress;
    /**
     * 是拓扑图配置的 还是自动发现的
     * 1拓扑图配置的，2自动发现的，3手动录入的
     * 自动发现的对端设备信息可以修改，拓扑图配置的对端设备信息不可以修改
     */
    private String maOrAt;

}
