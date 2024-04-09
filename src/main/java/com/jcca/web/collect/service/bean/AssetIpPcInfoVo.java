package com.jcca.web.collect.service.bean;

import lombok.Data;

@Data
public class AssetIpPcInfoVo {
    //vlan的mac地址
    private String mac;
    //端口的索引
    private String portIndex;
    //端口或者vlan上的IP
    private String portIp;
    //设备IP
    private String assetIp;
    //资产名称
    private String assetName;
    //资产端口名称
    private String portName;

    private String assetId;
    //资产类型
    private Integer assetType;


}
