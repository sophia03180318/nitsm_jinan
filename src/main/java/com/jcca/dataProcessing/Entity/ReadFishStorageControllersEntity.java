package com.jcca.dataProcessing.Entity;

import lombok.Data;

import java.util.List;

/**
 * 存储控制器列表
 */
@Data
public class ReadFishStorageControllersEntity {
    /**
     * 资产标签
     */
    private String assetTag;
    /**
     * 控制卡制造商
     */
    private String cardManufacturer;
    /**
     * 控制卡型号
     */
    private String cardModel;
    /**
     * 固件版本
     */
    private String firmwareVersion;
    /**
     * 制造商（同 CardManufacturer）
     */
    private String manufacturer;
    /**
     * 成员 ID
     */
    private String memberId;
    /**
     * 型号（同 CardModel）
     */
    private String model;
    /**
     * 控制器显示名称
     */
    private String name;
    /**
     * 控制器序列号
     */
    private String serialNumber;

    /**
     * 接口速度
     */
    private Integer speedGbps;
    /**
     * 控制器状态
     */
    private ReadFishStatusEntity status;
    /**
     * 支持的设备协议
     */
    private List<String> supportedDeviceProtocols;
}
