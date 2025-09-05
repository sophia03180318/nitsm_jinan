package com.jcca.dataProcessing.Entity;


import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 采集管理口的CPU信息
 */
@Data
public class CollectBhmMemoryEntity extends CommonEntity implements Serializable {

    /**
     * 内存设备在本地的简短唯一标识
     */
    private String id;
    /**
     * 内存设备的显示名称
     */
    private String name;
    /**
     * 内存模块类型
     */
    private String baseModuleType;
    /**
     * 内存技术类型
     * eg：DDR5（第五代双倍数据速率内存，相比 DDR4 提升带宽、降低功耗）。
     */
    private String memoryDeviceType;
    /**
     * 内存容量(MiB)
     * 1 GiB = 1024 MiB。
     */
    private Integer capacityMiB;
    /**
     * 数据位宽
     * eg: 64 位（内存传输数据的基础位宽，主流服务器内存标准）。
     */
    private Integer dataWidthBits;
    /**
     * 总线位宽
     * eg：80 位（= 64 位数据位宽 + 16 位 ECC 校验位），对应下文的 SingleBitECC 纠错能力。
     */
    private Integer busWidthBits;
    /**
     * 内存 支持的最高核心频率：
     * DDR 内存 “等效频率” 为核心频率 ×2
     */
    private List<Integer> allowedSpeedsMHz;
    /**
     * 纠错能力
     * SingleBitECC:单比特纠错（可自动修复单比特错误，检测多比特错误，服务器内存核心可靠性功能）。
     */
    private String errorCorrection;
    /**
     * 内存 Rank 数
     * 2 Rank（Rank 是内存芯片的 “逻辑组”，2 Rank 通常比 1 Rank 支持更高容量 / 兼容性）。
     */
    private Integer rankCount;
    /**
     * 内存厂商
     */
    private String manufacturer;
    /**
     * 内存厂商型号（料号）
     */
    private String partNumber;
    /**
     * 内存唯一序列号用于溯源（生产批次、保修）和区分同型号内存。
     */
    private String serialNumber;
    /**
     * 内存当前 核心运行频率：2200 MHz
     * （等效传输速率 = 2200 × 2 = 4400 MT/s，与 Oem 字段一致）。
     */
    private Integer operatingSpeedMhz;

    private ReadFishStatusEntity status;
    /**
     * Rank 备用功能
     * 该功能允许用备用 Rank 替换故障 Rank，提升可靠性）。
     */
    private Boolean isRankSpareEnabled;
    /**
     * 备用设备功能：
     * 该功能允许用备用内存替换故障内存。
     */
    private Boolean isSpareDeviceEnabled;
    /**
     * 剩余寿命
     */
    private String remainingServiceLifePercent;



}
