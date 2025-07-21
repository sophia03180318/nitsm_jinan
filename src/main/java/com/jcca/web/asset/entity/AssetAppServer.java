package com.jcca.web.asset.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * @author: hhw
 * @description: AssetAppServer 主要是用来
 * @date: 2025-07-03  16:26
 * @since: 2.0.15.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("ASSET_APP_SERVER")
public class AssetAppServer extends Model<AssetAppServer> {
    private static final long serialVersionUID = 6295322192850551457L;
    @TableId(value = "ID", type = IdType.ID_WORKER_STR)
    private String id;
    private String assetId;
    private String assetName;
    private Integer serverPort;
    private String linkAssetName;
    private String linkIp;
    /**
     * 0丢失，1正常，2未连接
     */
    private Integer linkStatus;

    /**
     * 创建时间
     */
    @TableField(value = "CREATE_TIME", fill = FieldFill.INSERT)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
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
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date modifyTime;
    /**
     * 修改者
     */
    @TableField(value = "MODIFIER", fill = FieldFill.INSERT_UPDATE)
    private String modifier;

}
