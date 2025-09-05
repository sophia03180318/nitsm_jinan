package com.jcca.dataProcessing.Entity;


import lombok.Data;

import java.io.Serializable;

/**
 * 采集管理口的CPU信息
 */
@Data
public class CollectBhmPowerEntity extends CommonEntity implements Serializable {

    /**
     * 名称
     */
    private String name;
    /**
     * 电源序列号
     */
    private String serialNumber;
    /**
     * 唯一标识
     * 内部的标识
     */
    private String memberId;
    /**
     * 型号
     */
    private String model;
    /**
     * 电源制造商
     */
    private String manufacturer;
    /**
     * 电源转换效率（交流输入功率转换为直流输出功率的比例）
     * 百分比
     */
    private Integer efficiencyPercent;

    /**
     * 源模块的固件版本号，用于标识软件版本
     */
    private String firmwareVersion;

    /**
     * 当前输入电压
     * 伏特
     */
    private String lineInputVoltage;

    /**
     * 输入电压类型
     * 交流、直流
     */
    private String lineInputVoltageType;

    /**
     * 输入电流
     */
    private String inputCurrent;

    /**
     * 输入功率
     */
    private String inputPower;

    /**
     * 输出电流
     */
    private String outputCurrent;

    /**
     * 输出电压
     */
    private String outputVolt;

    /**
     * 电源模块的最大容量
     * 额定最大功率 瓦特
     */
    private String powerCapacityWatts;
    /**
     * 当前输出功率
     * 实际供给服务器的功率
     */
    private String powerOutputWatts;

    /**
     * 电源类型（AC 交流，DC 直流）
     *
     */
    private String powerSupplyType;
    /**
     * 是否已经安装
     * true 物理上已经安装
     * false 没有安装
     */
    private Boolean present;
    /**
     * 状态
     */
    private ReadFishStatusEntity status;


}
