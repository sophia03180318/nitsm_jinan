package com.jcca.web2.entity;


import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * PCIE信息
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("COLLECT_BHM_PCIE_INFO")
public class CollectBhmPcieInfo extends Model<CollectBhmPcieInfo> {


    @TableId(value = "ID", type = IdType.ID_WORKER_STR)
    private String id;

    @TableField("ASSET_ID")
    private String assetId;

    /**
     * 该 PCIe 设备在当前 “PCIeDevices 资源列表” 中的本地唯一标识，示例中 0 表示是列表中的第 1 个设备（部分场景下可能用字符串格式）。
     */
    @TableField("PCIE_ID")
    private String pcieId;
    /**
     * 设备在 Redfish 接口中的显示名称，
     * 示例中 PCIeCard0 直观表示 “第 0 号 PCIe 卡”，便于管理界面展示。
     */
    @TableField("NAME")
    private String name;
    /**
     * 设备的完整产品名称，比 Model 更详细，包含厂商、功能、规格等信息。
     * 可直接判断设备类型和用途。
     */
    @TableField("PRODUCT_NAME")
    private String productName;
    /**
     * PCIe 卡的硬件厂商
     */
    @TableField("CARD_MANUFACTURER")
    private String cardManufacturer;
    /**
     * 与 CardManufacturer 含义一致
     */
    @TableField("MANUFACTURER")
    private String manufacturer;
    /**
     * PCIe 卡的具体型号
     */
    @TableField("CARD_MODEL")
    private String cardModel;
    /**
     * 与 CardModel 含义一致（冗余字段，均指向设备具体型号）
     */
    @TableField("MODEL")
    private String model;
    /**
     * 设备的简短描述，通常与型号一致
     */
    @TableField("DESCRIPTION")
    private String description;
    /**
     * 当前 PCIe 链路的实际通信速率世代（GenX 是 PCIe 速率的标准表述）：
     * - Gen1=2.5Gbps/ lane，Gen2=5Gbps/lane，Gen3=8Gbps/lane
     * 示例中 Gen3 表示当前链路运行在 Gen3 速率，单 lane 速率 8Gbps。
     */
    @TableField("CURRENT_LINK_SPEED")
    private String currentLinkSpeed;
    /**
     * 当前 PCIe 链路的实际通道数（xN 表示 N 个通道并行通信）：
     * 示例中 x8 表示当前使用 8 个 lane 通信，总带宽 = 单 lane 速率 × 通道数（双向，需 ×2），即 8Gbps ×8 ×2 = 128Gbps。
     */
    @TableField("CURRENT_LINK_WIDTH")
    private String currentLinkWidth;
    /**
     * PCIe 设备支持的最大速率世代（硬件上限）：
     * 示例中 Gen3 表示该设备最高仅支持 Gen3，无法升级到 Gen4/Gen5。
     */
    @TableField("MAX_LINK_SPEED")
    private String maxLinkSpeed;
    /**
     * PCIe 设备支持的最大通道数（硬件上限）：
     * 示例中 x8 表示该设备最多支持 8 个 lane，即使插入 x16 插槽，也只能运行在 x8 宽度。
     */
    @TableField("MAX_LINK_WIDTH")
    private String maxLinkWidth;
    /**
     * IANA 厂商编号（由互联网号码分配机构 IANA 分配给硬件厂商的唯一标识）：示例中 108 对应 Emulex 的厂商编号，用于系统识别厂商身份（避免厂商名称重复导致的混淆）。
     */
    @TableField("IANA")
    private Long iana;
    /**
     * 该设备当前实际占用的 PCIe 物理通道数（
     * 部分场景下与 CurrentLinkWidth 一致，示例中 1 可能是厂商自定义逻辑，需结合具体硬件判断，通常与链路宽度关联）。
     */
    @TableField("LANES")
    private Integer lanes;
    /**
     * PCIe 设备的功能类型（厂商或 Redfish 定义）：
     * 示例中 Fibre Channel 表示该设备是 “光纤通道（FC）设备”
     * ，明确设备用途（用于连接 FC 存储阵列）。
     */
    @TableField("PCIE_TYPE")
    private String pcieType;

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
