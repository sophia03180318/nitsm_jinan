package com.jcca.dataProcessing.Entity;


import lombok.Data;

/**
 * 红鱼协议读出的磁盘信息
 */
@Data
public class ReadFishDiskEntity {

    /**
     * 硬盘在系统中的唯一标识符
     */
    private String id;
    /**
     * 硬盘的显示名称
     */
    private String name;
    /**
     * 硬盘指示灯状态
     * 用于物理定位硬盘（如故障时闪烁提示）
     */
    private String indicatorLED;
    /**
     * 硬盘制造商
     */
    private String manufacturer;
    /**
     * 介质错误计数
     */
    private Integer mediaErrCount;
    /**
     * 存储介质类型
     */
    private String mediaType;
    /**
     * 硬盘型号
     */
    private String model;
    /**
     * 实际协商的传输速度（Gb/s）
     */
    private Double negotiatedSpeedGbs;
    /**
     * 接口协议
     */
    private String protocol;
    /**
     * 硬盘固件版本
     */
    private String revision;
    /**
     * 硬盘唯一序列号
     */
    private String serialNumber;

    private ReadFishStatusEntity status;
    /**
     * 状态指示器简化描述
     * 直观展示硬盘状态（与 Health 对应）
     */
    private String statusIndicator;
}
