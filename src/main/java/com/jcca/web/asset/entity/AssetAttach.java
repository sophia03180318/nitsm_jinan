package com.jcca.web.asset.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 资产附属信息表
 *
 * @author hanwone
 * @date 2020-04-26 15:49:18
 **/
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("asset_attach")
public class AssetAttach extends Model<AssetAttach> implements java.io.Serializable {

    private static final long serialVersionUID = 1L;


    /**
     * 资产ID
     */
    @TableId(value = "ASSET_ID", type = IdType.NONE)
    private String assetId;
    /**
     * 组织ID
     */
    @TableField("ORG_ID")
    private String orgId;
    /**
     * 机房ID
     */
    @TableField("ROOM_ID")
    private String roomId;
    /**
     * 机柜ID
     */
    @TableField("CABINET_ID")
    private String cabinetId;
    /**
     * 起始位置
     */
    @TableField("START_POSITION")
    private Integer startPosition;
    /**
     * 结束位置
     */
    @TableField("END_POSITION")
    private Integer endPosition;
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

}