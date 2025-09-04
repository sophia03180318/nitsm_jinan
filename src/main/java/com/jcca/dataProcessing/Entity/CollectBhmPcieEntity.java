package com.jcca.dataProcessing.Entity;


import cn.hutool.core.annotation.Alias;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 采集管理口的CPU信息
 */
@Data
public class CollectBhmPcieEntity extends CommonEntity implements Serializable {

    /**
     * 该 PCIe 设备在当前 “PCIeDevices 资源列表” 中的本地唯一标识，示例中 0 表示是列表中的第 1 个设备（部分场景下可能用字符串格式）。
     */
    private String id;
    /**
     * 设备在 Redfish 接口中的显示名称，
     * 示例中 PCIeCard0 直观表示 “第 0 号 PCIe 卡”，便于管理界面展示。
     */
    private String name;
    /**
     * 设备的完整产品名称，比 Model 更详细，包含厂商、功能、规格等信息。
     * 可直接判断设备类型和用途。
     */
    private String productName;
    /**
     * PCIe 卡的硬件厂商
     */
    private String cardManufacturer;
    /**
     * 与 CardManufacturer 含义一致
     */
    private String manufacturer;
    /**
     * PCIe 卡的具体型号
     */
    private String cardModel;
    /**
     * 与 CardModel 含义一致（冗余字段，均指向设备具体型号）
     */
    private String model;
    /**
     * 设备的简短描述，通常与型号一致
     */
    private String description;
    /**
     * 当前 PCIe 链路的实际通信速率世代（GenX 是 PCIe 速率的标准表述）：
     * - Gen1=2.5Gbps/ lane，Gen2=5Gbps/lane，Gen3=8Gbps/lane
     * 示例中 Gen3 表示当前链路运行在 Gen3 速率，单 lane 速率 8Gbps。
     */
    private String currentLinkSpeed;
    /**
     * 当前 PCIe 链路的实际通道数（xN 表示 N 个通道并行通信）：
     * 示例中 x8 表示当前使用 8 个 lane 通信，总带宽 = 单 lane 速率 × 通道数（双向，需 ×2），即 8Gbps ×8 ×2 = 128Gbps。
     */
    private String currentLinkWidth;
    /**
     * PCIe 设备支持的最大速率世代（硬件上限）：
     * 示例中 Gen3 表示该设备最高仅支持 Gen3，无法升级到 Gen4/Gen5。
     */
    private String maxLinkSpeed;
    /**
     * PCIe 设备支持的最大通道数（硬件上限）：
     * 示例中 x8 表示该设备最多支持 8 个 lane，即使插入 x16 插槽，也只能运行在 x8 宽度。
     */
    private String maxLinkWidth;
    /**
     * IANA 厂商编号（由互联网号码分配机构 IANA 分配给硬件厂商的唯一标识）：示例中 108 对应 Emulex 的厂商编号，用于系统识别厂商身份（避免厂商名称重复导致的混淆）。
     */
    private Long iana;
    /**
     * 该设备当前实际占用的 PCIe 物理通道数（
     * 部分场景下与 CurrentLinkWidth 一致，示例中 1 可能是厂商自定义逻辑，需结合具体硬件判断，通常与链路宽度关联）。
     */
    private Integer lanes;
    /**
     * PCIe 设备的功能类型（厂商或 Redfish 定义）：
     * 示例中 Fibre Channel 表示该设备是 “光纤通道（FC）设备”
     * ，明确设备用途（用于连接 FC 存储阵列）。
     */
    private String pcieType;
    /**
     * 状态
     */
    private ReadFishStatusEntity status;


}
