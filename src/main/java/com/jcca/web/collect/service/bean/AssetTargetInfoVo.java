package com.jcca.web.collect.service.bean;

import lombok.Data;

import java.util.List;

@Data
public class AssetTargetInfoVo {
    //本地端口mac地址
    //对端设备Id
    private String localPortMac;
    //本地端口索引
    private String localPortIndex;
    //本地资产id
    private String localAssetId;
    //本地的IP
    private String assetIp;

    //本地端口名称
    private String localPortIndexName;
    //本地端口配置的IP
    private String localPortIp;

    List<TargetInfoVo> targetInfoVos;
    //portChannel对应的真实端口索引
    private String portChannelTargetIndex;
}
