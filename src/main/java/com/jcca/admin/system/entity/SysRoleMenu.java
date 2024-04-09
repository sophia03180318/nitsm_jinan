package com.jcca.admin.system.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 角色菜单表
 *
 * @author hanwone
 * @date 2020-04-10 09:51:11
 **/
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("sys_role_menu")
public class SysRoleMenu extends Model<SysRoleMenu> implements java.io.Serializable {

    private static final long serialVersionUID = 1L;


    /**
     * 角色ID
     */
    @TableId
    private String roleId;
    /**
     * 菜单ID
     */
    @TableId
    private String menuId;

}