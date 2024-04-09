package com.jcca.admin.system.dao;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jcca.admin.system.entity.SysRoleMenu;

/**
 * @author hanwone
 * @date 2020-04-10 09:51:11
 **/
public interface SysRoleMenuMapper extends BaseMapper<SysRoleMenu> {


    /**
     * 根据角色ID删除角色菜单关联关系
     *
     * @param roleId
     */
    void deleteByRoleId(String roleId);
}
