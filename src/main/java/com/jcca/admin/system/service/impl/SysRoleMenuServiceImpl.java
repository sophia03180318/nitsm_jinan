package com.jcca.admin.system.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcca.admin.system.dao.SysRoleMenuMapper;
import com.jcca.admin.system.entity.SysRoleMenu;
import com.jcca.admin.system.service.SysRoleMenuService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author hanwone
 * @date 2020-04-10 09:51
 **/
@Service
public class SysRoleMenuServiceImpl extends ServiceImpl<SysRoleMenuMapper, SysRoleMenu> implements SysRoleMenuService {

    @Resource
    private SysRoleMenuService roleMenuService;

    @Resource
    private SysRoleMenuMapper roleMenuMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int saveMenus(String roleId, List<String> menuIds) {
        roleMenuMapper.deleteByRoleId(roleId);

        if (menuIds != null) {
            List<SysRoleMenu> roleMenuList = new ArrayList<>();
            for (String menuId : menuIds) {
                SysRoleMenu rm = new SysRoleMenu();
                rm.setRoleId(roleId);
                rm.setMenuId(menuId);
                roleMenuList.add(rm);
            }
            roleMenuService.saveBatch(roleMenuList);
            return menuIds.size();
        }
        return 0;
    }
}