package com.jcca.web.topo.controller.bean;

import lombok.Data;

/**
 * topo 发现结果
 *
 * @author lyp
 */
@Data
public class TopoFindResp {
    /**
     * 发起TOPO发现的资产IP
     */
    private String localhostIp;
    /**
     * 本地端口索引
     */
    private String localPortIndex;

    private String localAssetId;
    /**
     * 链接资产名称
     */
    private String remoteAssetName;
    /**
     * 连接远程端口索引
     */
    private String remotePortIndex;
    /**
     * 链接远程管理口IP
     */
    private String remoteIp;
    /**
     * 链接远程端口IP
     */
    private String remotePortIp;

    private String remoteAssetId;
}
