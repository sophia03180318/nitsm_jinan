package com.jcca.web.asset.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 资产硬件更换记录表
 *
 * @author hanwone
 * @date 2020-07-16 18:05:21
 **/
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("asset_hardware_fix")
public class AssetHardwareFix extends Model<AssetHardwareFix> implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 数据ID
     */
    @TableId(value = "id", type = IdType.ID_WORKER_STR)
    private String id;
    /**
     * 资产ID
     */
    @TableField("ASSET_ID")
    private String assetId;
    /**
     * 原FRU号码
     */
    @TableField("FRU_BEFORE")
    private String fruBefore;
    /**
     * 现FRU号码
     */
    @TableField("FRU_NOW")
    private String fruNow;
    /**
     * 硬件类别
     * AssetHardwareTypeEnum
     */
    @TableField("HARDWARE_TYPE")
    private Integer hardwareType;
    /**
     * 更换时间
     */
    @TableField("FIX_TIME")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date fixTime;
    /**
     * 故障原因
     */
    @TableField("REASON")
    private String reason;
    /**
     * 故障现象
     */
    @TableField("DESCRIPTION")
    private String description;
    /**
     * 中心签名
     */
    @TableField("CENTER_SIGNATURE")
    private String centerSignature;
    /**
     * 厂家签名
     */
    @TableField("SUPPLIER_SIGNATURE")
    private String supplierSignature;
    /**
     * 创建时间
     */
    @TableField(value = "CREATE_TIME", fill = FieldFill.INSERT)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
    /**
     * 创建人
     */
    @TableField(value = "CREATOR", fill = FieldFill.INSERT)
    private String creator;
    /**
     * 修改时间
     */
    @TableField(value = "MODIFY_TIME", fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date modifyTime;
    /**
     * 修改人
     */
    @TableField(value = "MODIFIER", fill = FieldFill.INSERT_UPDATE)
    private String modifier;

}