package com.jcca.web2.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.jcca.dataProcessing.Entity.ReadFishStatusEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;
import java.util.List;

/**
 * 存储组信息
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("COLLECT_BHM_STORAGE_CONTROLLERS_INFO")
public class CollectBhmStorageControllersInfo extends Model<CollectBhmStorageControllersInfo> {

    @TableId(value = "ID", type = IdType.ID_WORKER_STR)
    private String id;

    @TableField("ASSET_ID")
    private String assetId;
    /**
     * 组ID
     */
    @TableField("STORAGE_ID")
    private String storageId;

    /**
     * 资产标签
     */
    @TableField("ASSET_TAG")
    private String assetTag;
    /**
     * 控制卡制造商
     */
    @TableField("CARD_MANUFACTURER")
    private String cardManufacturer;
    /**
     * 控制卡型号
     */
    @TableField("CARD_MODEL")
    private String cardModel;
    /**
     * 固件版本
     */
    @TableField("FIRMWARE_VERSION")
    private String firmwareVersion;
    /**
     * 制造商（同 CardManufacturer）
     */
    @TableField("MANUFACTURER")
    private String manufacturer;
    /**
     * 成员 ID
     */
    @TableField("MEMBER_ID")
    private String memberId;
    /**
     * 型号（同 CardModel）
     */
    @TableField("MODEL")
    private String model;
    /**
     * 控制器显示名称
     */
    @TableField("NAME")
    private String name;
    /**
     * 控制器序列号
     */
    @TableField("SERIAL_NUMBER")
    private String serialNumber;
    /**
     * 接口速度
     */
    @TableField("SPEED_GBPS")
    private Integer speedGbps;
    /**
     * 支持的设备协议
     * list
     */
    @TableField("SUPPORTED_DEVICE_PROTOCOLS")
    private String supportedDeviceProtocols;



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
