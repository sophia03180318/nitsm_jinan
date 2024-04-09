package com.jcca.web.asset.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotEmpty;
import java.util.Date;

/**
 * 机房表
 *
 * @author hanwone
 * @date 2020-04-26 15:47:59
 **/
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("room")
public class Room extends Model<Room> implements java.io.Serializable {

    private static final long serialVersionUID = 1L;


    /**
     * 数据ID
     */
    @TableId(value = "id", type = IdType.ID_WORKER_STR)
    private String id;
    /**
     * 机房名称
     */
    @TableField("NAME")
    @NotEmpty(message = "机房名称不能为空")
    private String name;
    /**
     * 组织ID
     */
    @TableField("ORG_ID")
    @NotEmpty(message = "组织机构不能为空")
    private String orgId;
    /**
     * 备注
     */
    @TableField("REMARK")
    private String remark;
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

    @TableField(exist = false)
    private String orgName;

    /**
     * 页面所筛选的组织树回传id 与机柜无直接关系
     */
    @TableField(exist = false)
    private String orgTreeId;
}