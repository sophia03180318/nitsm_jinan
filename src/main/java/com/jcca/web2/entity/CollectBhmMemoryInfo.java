package com.jcca.web2.entity;


import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.jcca.dataProcessing.Entity.ReadFishStatusEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;
import java.util.List;

/**
 * 内存
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("COLLECT_BHM_MEMORY_INFO")
public class CollectBhmMemoryInfo extends Model<CollectBhmMemoryInfo> {


    @TableId(value = "ID", type = IdType.ID_WORKER_STR)
    private String id;

    @TableField("ASSET_ID")
    private String assetId;

    /**
     * 内存设备在本地的简短唯一标识
     */
    @TableField("MEMORY_ID")
    private String memoryId;
    /**
     * 内存设备的显示名称
     */
    @TableField("NAME")
    private String name;
    /**
     * 内存模块类型
     */
    @TableField("BASE_MODULE_TYPE")
    private String baseModuleType;
    /**
     * 内存技术类型
     * eg：DDR5（第五代双倍数据速率内存，相比 DDR4 提升带宽、降低功耗）。
     */
    @TableField("MEMORY_DEVICE_TYPE")
    private String memoryDeviceType;
    /**
     * 内存容量(MiB)
     * 1 GiB = 1024 MiB。
     */
    @TableField("CAPACITY_MIB")
    private Integer capacityMiB;
    /**
     * 数据位宽
     * eg: 64 位（内存传输数据的基础位宽，主流服务器内存标准）。
     */
    @TableField("DATA_WIDTH_BITS")
    private Integer dataWidthBits;
    /**
     * 总线位宽
     * eg：80 位（= 64 位数据位宽 + 16 位 ECC 校验位），对应下文的 SingleBitECC 纠错能力。
     */
    @TableField("BUS_WIDTH_BITS")
    private Integer busWidthBits;
    /**
     * 内存 支持的最高核心频率：
     * DDR 内存 “等效频率” 为核心频率 ×2
     */
    @TableField("ALLOWED_SPEEDS_MHZ")
    private String allowedSpeedsMHz;
    /**
     * 纠错能力
     * SingleBitECC:单比特纠错（可自动修复单比特错误，检测多比特错误，服务器内存核心可靠性功能）。
     */
    @TableField("ERROR_CORRECTION")
    private String errorCorrection;
    /**
     * 内存 Rank 数
     * 2 Rank（Rank 是内存芯片的 “逻辑组”，2 Rank 通常比 1 Rank 支持更高容量 / 兼容性）。
     */
    @TableField("RANK_COUNT")
    private Integer rankCount;
    /**
     * 内存厂商
     */
    @TableField("MANUFACTURER")
    private String manufacturer;
    /**
     * 内存厂商型号（料号）
     */
    @TableField("PART_NUMBER")
    private String partNumber;
    /**
     * 内存唯一序列号用于溯源（生产批次、保修）和区分同型号内存。
     */
    @TableField("SERIAL_NUMBER")
    private String serialNumber;
    /**
     * 内存当前 核心运行频率：2200 MHz
     * （等效传输速率 = 2200 × 2 = 4400 MT/s，与 Oem 字段一致）。
     */
    @TableField("OPERATING_SPEED_MHZ")
    private Integer operatingSpeedMhz;
    /**
     * Rank 备用功能
     * 该功能允许用备用 Rank 替换故障 Rank，提升可靠性）。
     * 启用/未启用
     */
    @TableField("IS_RANK_SPARE_ENABLED")
    private String isRankSpareEnabled;
    /**
     * 备用设备功能：
     * 该功能允许用备用内存替换故障内存。
     * 启用/未启用
     */
    @TableField("IS_SPARE_DEVICE_ENABLED")
    private String isSpareDeviceEnabled;
    /**
     * 剩余寿命
     */
    @TableField("REMAINING_SERVICE_LIFE_PERCENT")
    private String remainingServiceLifePercent;


    /**
     * 健康状态
     */
    @TableField("HEALTH")
    private String health;
    /**
     * 是否启用
     * Enabled 启用
     */
    @TableField("STATE")
    private String state;

    /**
     * 采集批次码
     */
    @TableField("COLLECT_CODE")
    private String collectCode;
    /**
     * 创建时间
     */
    @TableField(value = "CREATE_TIME", fill = FieldFill.INSERT)
    private Date createTime;

}
