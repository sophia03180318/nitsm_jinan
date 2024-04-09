package com.jcca.web.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 业务组织关系表
 *
 * @author hanwone
 * @date 2020-05-29 10:11:57
 **/
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_manage")
public class BizManage extends Model<BizManage> implements java.io.Serializable {

    private static final long serialVersionUID = 1L;


    /**
     * 数据ID
     */
    @TableId(value = "id", type = IdType.ID_WORKER_STR)
    private String id;
    /**
     * 组织ID
     */
    @TableField("MANAGE_ID")
    private String manageId;
    /**
     * 业务ID
     */
    @TableField("BIZ_ID")
    private String bizId;
    /**
     * 业务类型
     */
    @TableField("BIZ_TYPE")
    private int bizType;
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