package com.jcca.web.collect.service.bean;

import lombok.Data;

@Data
public class TopoRouteTarget {
    /**
     * 对端IP
     */
    private String atNetAddress;
    /**
     * 对端物理地址
     */
    private String atPhysAddress;

    /**
     * 对端资产ID
     */
    private String atAssetId;

    /**
     * 对端资产名称
     */
    private String atName;
    /**
     * 对端端口名称或网卡名称
     */
    private String atPortName;


    private String atPortIndexName;
    /**
     * 对端设备类型
     */
    private Integer atType;
}
