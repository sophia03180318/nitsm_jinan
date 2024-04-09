package com.jcca.admin.system.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 角色组织表，对应前端各组织设备管理
 *
 * @author hanwone
 * @date 2020-04-13 10:42:26
 **/
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("sys_role_org")
public class SysRoleOrg extends Model<SysRoleOrg> implements java.io.Serializable {

    private static final long serialVersionUID = 1L;


    /**
     * 角色ID
     */
    @TableId
    private String roleId;
    /**
     * 组织ID
     */
    @TableId
    private String orgId;

}