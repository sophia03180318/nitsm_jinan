package com.jcca.web.db.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 数据库配置信息
 *
 * @author Lvyp
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("M_DB")
public class ManageDb extends Model<ManageDb> {

    private static final long serialVersionUID = 1L;

    @TableId(value = "ID", type = IdType.ID_WORKER_STR)
    private String id;
    /**
     * 资产ID
     */
    @TableField("ASSET_ID")
    private String assetId;
    /**
     * 数据库名称
     */
    @TableField("NAME")
    private String name;
    /**
     * 数据库实例名称
     */
    @TableField("DB_NAME")
    private String dbName;
    /**
     * 数据库类型 DBTypeEnum
     */
    @TableField("DB_TYPE")
    private Byte dbType;
    /**
     * 数据库协议 DBTypeEnum
     */
    @TableField("DB_PROTOCOL")
    private Byte dbProtocol;

    @TableField("MANUFACTURER_ID")
    private Integer manufacturerId;
    /**
     * 端口
     */
    @TableField("PORT")
    private Integer port;
    /**
     * 账号
     * 密文
     */
    @TableField("USERNAME")
    private String username;
    /**
     * 密码
     * 密文
     */
    @TableField("PASSWORD")
    private String password;
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
