package com.jcca.admin.system.dao;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jcca.admin.system.entity.SysMenu;
import com.jcca.common.bean.constant.StatusConst;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Set;

/**
 * @author hanwone
 * @date 2020-04-06 12:14:51
 **/
public interface SysMenuMapper extends BaseMapper<SysMenu> {


    /**
     * 根据角色ID获取菜单列表
     *
     * @param roleId
     * @return
     */
    List<SysMenu> findMenuByRoleId(String roleId);

    /**
     * 获取最大的排序数
     *
     * @param pid
     * @return
     */
    Byte getMenuSortMax(String pid);

    /**
     * 根据登录用户获取用户权限列表
     *
     * @param userId
     * @return
     */
    Set<String> getPermsByUserId(String userId);

    /**
     * 获取全部
     *
     * @return
     */
    Set<String> getAllPerms();

    Set<String> getDirsByUserId(String userId);
}
