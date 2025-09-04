package com.jcca.web2.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.jcca.dataProcessing.Entity.ReadFishStatusEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 存储组信息
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("COLLECT_BHM_S_DISK_INFO")
public class CollectBhmStorageDiskInfo extends Model<CollectBhmStorageDiskInfo> {

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
     * 硬盘在系统中的唯一标识符
     */
    @TableField("DISK_ID")
    private String diskId;
    /**
     * 硬盘的显示名称
     */
    @TableField("NAME")
    private String name;
    /**
     * 硬盘指示灯状态
     * 用于物理定位硬盘（如故障时闪烁提示）
     */
    @TableField("INDICATOR_LED")
    private String indicatorLED;
    /**
     * 硬盘制造商
     */
    @TableField("MANUFACTURER")
    private String manufacturer;
    /**
     * 介质错误计数
     */
    @TableField("MEDIA_ERR_COUNT")
    private Integer mediaErrCount;
    /**
     * 存储介质类型
     */
    @TableField("MEDIA_TYPE")
    private String mediaType;
    /**
     * 硬盘型号
     */
    @TableField("DISK_MODEL")
    private String model;
    /**
     * 实际协商的传输速度（Gb/s）
     */
    @TableField("NEGOTIATED_SPEED_GBS")
    private Double negotiatedSpeedGbs;
    /**
     * 接口协议
     */
    @TableField("PROTOCOL")
    private String protocol;
    /**
     * 硬盘固件版本
     */
    @TableField("REVISION")
    private String revision;
    /**
     * 硬盘唯一序列号
     */
    @TableField("SERIAL_NUMBER")
    private String serialNumber;
    /**
     * 状态指示器简化描述
     * 直观展示硬盘状态（与 Health 对应）
     */
    @TableField("STATUS_INDICATOR")
    private String statusIndicator;

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
