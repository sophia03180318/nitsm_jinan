package com.jcca.web.collect.service.bean;

import lombok.Data;

@Data
public class TargetInfoVo {
    //所属VLAN索引
    private String belongVlanIndex;
    //所属vlan
    private String belongVlan;
    //本地的IP
    private String localIp;
    //对端设备资产IP
    private String targetAssetIp;
    //关联设备端口的IP地址
    private String targetPortIp;
    //关联设备的Mac地址
    private String targetMac;
    //关联IP地址的类型dynamic(3)，static(4)
    private String type;
    //对端设备名称
    private String targetPort;

    private String collectType;
    //对端设备ID
    private String targetAssetId;
}
