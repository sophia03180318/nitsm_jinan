package com.jcca.web.collect.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 板卡采集数据
 *
 * @author Lvyp
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("COLLECT_PCB")
public class CollectPcb extends Model<CollectPcb> {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ID_WORKER_STR)
    private String id;
    /**
     * 采集时间
     */
    @TableField("COLLECT_TIME")
    private Date collectTime;
    /**
     * 采集编号 同一台设备同一次采集编号相同
     */
    @TableField("COLLECT_CODE")
    private String collectCode;
    /**
     * 资产ID
     */
    @TableField("ASSET_ID")
    private String assetId;
    /**
     * 板卡名称
     */
    @TableField("NAME")
    private String name;
    /**
     * 板卡描述
     */
    @TableField("DESC_STR")
    private String descStr;
    /**
     * 模式名称
     */
    @TableField("MODEL_NAME")
    private String modelName;
    /**
     * 板卡类型 实体通用类型 entPhysicalClass
     */
    @TableField("TYPE")
    private String type;
    /**
     * 物理索引
     */
    @TableField("ENT_PHYSICAL_INDEX")
    private String entPhysicalIndex;
    /**
     * 直接父级物理索引物理索引 0表示顶级
     */
    @TableField("ENT_PHYSICAL_CONTAINED_IN")
    private String entPhysicalContainedIn;
    /**
     * 在父节点中的相对索引（排序编号） 子排序
     */
    @TableField("ENT_PHYSICAL_PAR_PELPOS")
    private String entPhysicalParRelPos;

    /**
     * 此模块是否支持插拔
     */
    @TableField("ENT_PHYSICAL_IS_FRU")
    private String entPhysicalIsFRU;

    /**
     * 序列号
     */
    @TableField("SERIAL_NUMBER")
    private String serialNumber;
    /**
     * 序列号名称
     */
    @TableField("SERIAL_NUMBER_NAME")
    private String serialNumberName;
    /**
     * 软件版本
     */
    @TableField("SOFTWARE_VERSION")
    private String softwareVersion;
    /**
     * 硬件版本
     */
    @TableField("HARDWATE_VERSION")
    private String hardwareVersion;
    /**
     * 系统版本
     */
    @TableField("OS_VERSION")
    private String osVersion;
    /**
     * 板卡索引号
     */
    @TableField("PCB_INDEX")
    private String pcbIndex;
    /**
     * 创建时间
     */
    @TableField(value = "CREATE_TIME", fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 板卡状态采集
     * 1: unknown (未知)
     * 2: up (正常工作)
     * 3: disabled (禁用)
     * 4: okButDiagFailed (诊断失败但基本功能正常)
     */
    @TableField("ENT_PHYSICAL_CARD_STATUS_REV")
    private String entPhysicalCardStatusRev;
}
