package com.jcca.component.client.bean;

import lombok.Data;

/**
 * 采集器中中心资产信息
 *
 * @author lyp
 */
@Data
public class CollectorCenterAssetMsg {

    /**
     * 资产ID
     */
    private String id;
    /**
     * 资产IP
     */
    private String ip;
    /**
     * 资产名称
     */
    private String name;
    /**
     * 资产类型
     */
    private String assetMode;
    /**
     * 资产分配的采集器节点URL
     */
    private String nodeUrl;
    /**
     * 0离线、1在线、2未知
     */
    private String pingStatus;

}
