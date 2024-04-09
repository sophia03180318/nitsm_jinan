package com.jcca.web.collect.service.bean;

import lombok.Data;

@Data
public class AssetIpInfoVo {
    //vlan的mac地址
    private String mac;
    //端口的索引
    private String portIndex;

    private String PortIndexName;
    //端口或者vlan上的IP
    private String portIp;
    //设备IP
    private String assetIp;

    private String assetName;

    private Integer assetType;

    private String assetId;


}
