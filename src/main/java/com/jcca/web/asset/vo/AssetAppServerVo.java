package com.jcca.web.asset.vo;

import lombok.Data;

import java.util.List;

/**
 * @author: hhw
 * @description: AssetAppServerVo 主要是用来
 * @date: 2025-07-04  11:49
 * @since: 2.0.15.0
 */
@Data
public class AssetAppServerVo {

    private String id;
    private String itemName;
    private String assetId;
    private String ip;
    private String serverPort;
    private String linkAssetName;
    private String linkIp;
    private String collectTime;
    private String alarmId;
    /**
     * 0丢失，1正常，2未连接
     */
    private Integer linkStatus;
    // 告警状态，1告警，2恢复
    private Byte alarmState;
    // cpu负载
    private Double loadOne;
    private Double loadFive;
    private Double loadFifteen;

    private List<AssetAppServerVo> children;
}
