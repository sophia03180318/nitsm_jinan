package com.jcca.admin.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;
import java.util.Date;
import java.util.List;

/**
 * 用户表
 *
 * @author hanwone
 * @date 2020-04-06 12:15:45
 **/
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("sys_user")
public class SysUser extends Model<SysUser> implements java.io.Serializable {

    private static final long serialVersionUID = 1L;


    /**
     * 用户ID
     */
    @TableId(value = "id", type = IdType.ID_WORKER_STR)
    private String id;
    /**
     * 用户名
     */
    @TableField("USERNAME")
    @NotEmpty(message = "用户名不能为空")
    @Length(max = 20, message = "内容不能超过20个汉字")
    private String username;
    /**
     * 昵称
     */
    @TableField("NICKNAME")
    @NotEmpty(message = "用户昵称不能为空")
    @Length(max = 20, message = "内容不能超过20个汉字")
    private String nickname;
    /**
     * 密码
     */
    @TableField("PASSWORD")
    private String password;
    @TableField(exist = false)
    private String confirm;
    /**
     * 密码盐
     */
    @TableField("PWD_SALT")
    private String pwdSalt;
    /**
     * 性别
     */
    @TableField("GENDER")
    private Byte gender;
    /**
     * 电话
     */
    @TableField("PHONE")
    private String phone;
    /**
     * 头像
     */
    @TableField("PICTURE")
    private String picture;
    /**
     * 备注
     */
    @TableField("REMARK")
    @Length(max = 42, message = "内容不能超过42个汉字")
    private String remark;
    /**
     * 用户状态
     */
    @TableField("STATUS")
    private Byte status;
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
     * 用户角色ID列表
     **/
    @TableField(exist = false)
    private List<String> roleIdList;
    @TableField(exist = false)
    private List<SysRole> roleList;
}