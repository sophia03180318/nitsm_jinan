package com.jcca.web2.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * @description: 资产型号
 * @author: sophia
 * @create: 2023/11/02 14:39
 **/
@EqualsAndHashCode(callSuper = true)
@Data
public class AssetModel extends Model<AssetModel> {

    private static final long serialVersionUID = -2664024094288650319L;
    /**
     * id
     */
    @TableId(value = "id", type = IdType.ID_WORKER_STR)
    private String id;

    /**
     * 型号名称
     */
    @TableField("MODEL")
    @NotEmpty(message = "型号不能为空")
    private String model;

    /**
     * 资产类型
     */
    @TableField("ASSET_MODE_ID")
    private String assetModeId;
    @TableField(exist = false)
    @NotNull(message = "资产类型不能为空")
    private Integer desk;
    /**
     * 图片路径
     */
    @TableField("PATH")
    private String path;

    /**
     * 图片名称
     */
    @TableField("FILE_NAME")
    private String fileName;

    /**
     * 资产厂商ID，关联ASSET_MANUFACTURER表
     */
    @TableField("MANUFACTURER_ID")
    @NotNull(message = "厂商不能为空")
    private Long manufacturerId;
    @TableField(exist = false)
    private String manufacturerName;
    /**
     * 供货商
     */
    @TableField("ASSET_SUPPLIER")
    private String assetSupplier;
    /**
     * 上架时间
     */
    @TableField("ONLINE_TIME")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date onlineTime;
    /**
     * 下架时间
     */
    @TableField("DOWNLINE_TIME")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date downlineTime;
    /**
     * 质保期限
     */
    @TableField("VALIDITY_DATE")
    private String validityDate;
    /**
     * 操作系统版本
     */
    @TableField("OPERATION_SYSTEM")
    private String operationSystem;
    /**
     * CPU型号
     */
    @TableField("CPU_MODEL")
    private String cpuModel;
    /**
     * CPU个数
     */
    @TableField("CPU_NUMBER")
    private Integer cpuNumber;
    /**
     * CPU核数
     */
    @TableField("CPU_CORE_NUMBER")
    private Integer cpuCoreNumber;
    /**
     * CPU主频
     */
    @TableField("CPU_FREQUENCY")
    private String cpuFrequency;
    /**
     * 电源模块个数
     */
    @TableField("POWER_TOTAL")
    private Integer powerTotal;
    /**
     * 电源模块型号
     */
    @TableField("POWER_MODEL")
    private String powerModel;
    /**
     * 单硬盘容量
     */
    @TableField("DISK_CAPACITY")
    private String diskCapacity;
    /**
     * 硬盘个数
     */
    @TableField("DISK_TOTAL")
    private Integer diskTotal;
    /**
     * 内存
     */
    @TableField("MEMORY")
    private String memory;
    /**
     * 占用U数
     */
    @TableField("ASSET_UNIT")
    @Max(value = 49, message = "U数不能大于49")
    @Min(value = 1, message = "U数不能小于1")
    private Integer assetUnit;
    /**
     * 调度台连接显示器数量
     */
    @TableField("DISPLAYER_TOTAL")
    private String displayerTotal;
    /**
     * 调度台视频接口类型
     */
    @TableField("DISPLAYER_PORT_MODEL")
    private String displayerPortModel;
    /**
     * 备注
     */
    @TableField("REMARK")
    private String remark;
    /**
     * 采集类型，0:linux,1:windows,2:aix,-1:网络设备
     * SystemTypeEnum
     */
    @TableField("COLLECTION_TYPE")
    private Integer collectionType;
    /**
     * 创建时间
     */
    @TableField(value = "CREATE_TIME", fill = FieldFill.INSERT)
    private Date createTime;
    /**
     * 创建者
     */
    @TableField(value = "CREATOR", fill = FieldFill.INSERT)
    private String creator;
    /**
     * 修改时间
     */
    @TableField(value = "MODIFY_TIME", fill = FieldFill.INSERT_UPDATE)
    private Date modifyTime;
    /**
     * 修改者
     */
    @TableField(value = "MODIFIER", fill = FieldFill.INSERT_UPDATE)
    private String modifier;

    /**
     * 资产类型
     */
    @TableField(exist = false)
    private String assetCode;

}