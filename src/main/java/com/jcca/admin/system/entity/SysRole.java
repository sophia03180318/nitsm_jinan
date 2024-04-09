package com.jcca.admin.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;
import java.util.Date;

/**
 * 角色表
 *
 * @author hanwone
 * @date 2020-04-06 12:15:20
 **/
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("sys_role")
public class SysRole extends Model<SysRole> implements java.io.Serializable {

    private static final long serialVersionUID = 1L;


    /**
     * 角色ID
     */
    @TableId(value = "id", type = IdType.ID_WORKER_STR)
    private String id;
    /**
     * 角色名称
     */
    @TableField("TITLE")
    @NotEmpty(message = "角色名称不能为空")
    @Length(max = 20, message = "角色名称不能超过20个字符")
    private String title;
    /**
     * 标识名称
     */
    @TableField("NAME")
    @NotEmpty(message = "角色标识不能为空")
    @Length(max = 10, message = "角色标识不能超过10个字符")
    private String name;
    /**
     * 父级ID
     */
    @TableField("PID")
    @NotEmpty(message = "父级角色不能为空")
    private String pid;
    /**
     * 所有父级ID
     */
    @TableField("PIDS")
    private String pids;
    /**
     * 排序
     */
    @TableField("SORT")
    private Byte sort;
    /**
     * 角色状态
     */
    @TableField("STATUS")
    private Byte status;
    /**
     * 备注
     */
    @TableField("REMARK")
    @Length(max = 42, message = "内容不能超过42个字符")
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