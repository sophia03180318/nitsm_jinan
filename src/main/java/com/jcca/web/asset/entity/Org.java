package com.jcca.web.asset.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotEmpty;
import java.util.Date;

/**
 * 组织表
 *
 * @author sophia
 **/
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("sys_org")
public class Org extends Model<Org> implements java.io.Serializable {

    private static final long serialVersionUID = 1L;


    /**
     * 组织ID
     */
    @TableId(value = "id", type = IdType.ID_WORKER_STR)
    private String id;
    /**
     * 组织名称
     */
    @TableField("TITLE")
    @NotEmpty(message = "组织名称不能为空")
    private String title;
    /**
     * 组织类型
     */
    @TableField("TYPE")
    @NotEmpty(message = "组织类型不能为空")
    private Integer type;
    /**
     * 父级ID
     */
    @TableField("PID")
    @NotEmpty(message = "父级ID不能为空")
    private String pid;
    /**
     * 所有父级ID
     */
    @TableField("PIDS")
    @NotEmpty(message = "所有父级id不能为空")
    private String pids;
    /**
     * 排序
     */
    @TableField("SORT")
    @NotEmpty(message = "排序不能为空")
    private Integer sort;
    /**
     * 状态
     */
    @TableField("STATUS")
    private Integer status;
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

}