package com.jcca.admin.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jcca.admin.system.entity.SysRoleMenu;

import java.util.List;

/**
 * @author hanwone
 * @date 2020-04-10 09:51:11
 **/
public interface SysRoleMenuService extends IService<SysRoleMenu> {


    /**
     * 保存角色菜单关系
     *
     * @param roleId
     * @param menuIds
     * @return
     */
    int saveMenus(String roleId, List<String> menuIds);
}
