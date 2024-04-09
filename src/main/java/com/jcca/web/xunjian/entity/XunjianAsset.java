package com.jcca.web.xunjian.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

import static com.fasterxml.jackson.annotation.JsonFormat.Shape;

/**
 * 待巡检设备表
 *
 * @author hanwone
 * @date 2021-02-24 16:09:21
 **/
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("xunjian_asset")
public class XunjianAsset extends Model<XunjianAsset> implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 数据ID
     */
    @TableId(value = "id", type = IdType.ID_WORKER_STR)
    private String id;
    /**
     * 巡视人
     */
    @TableField("OPERATOR")
    private String operator;
    /**
     * 设备ID
     */
    @TableField("ASSET_ID")
    private String assetId;
    /**
     * 设备名称
     */
    @TableField("ASSET_NAME")
    private String assetName;
    /**
     * 设备类型
     */
    @TableField("ASSET_MODE")
    private Integer assetMode;
    /**
     * 设备IP
     */
    @TableField("ASSET_IP")
    private String assetIp;
    /**
     * 设备IP2
     */
    @TableField("ASSET_IP2")
    private String assetIp2;
    /**
     * 设备位置
     */
    @TableField("ASSET_POSITION")
    private String assetPosition;
    /**
     * 生产厂商ID
     */
    @TableField(value = "MANUFACTURER_ID")
    private Integer manufactoryId;
    /**
     * 上架时间
     */
    @TableField(value = "ONLINE_TIME")
    private Date onlineTime;
    /**
     * 下架时间
     */
    @TableField(value = "DOWNLINE_TIME")
    @JsonFormat(shape = Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date downlineTime;

    /**
     * 安装位置
     */
    @TableField(value = "START_END_POSITION")
    private String startEndPosition;
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
    @JsonFormat(shape = Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date modifyTime;
    /**
     * 修改者
     */
    @TableField(value = "MODIFIER", fill = FieldFill.INSERT)
    private String modifier;
    /**
     * 1未删除
     * 0已删除
     */
    @TableField(value = "DELETE_FLAH", fill = FieldFill.INSERT)
    private Integer deleteFlag;

}