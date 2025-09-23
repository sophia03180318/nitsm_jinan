package com.jcca.admin.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jcca.admin.system.entity.SysMenu;
import com.jcca.common.enums.StatusEnum;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author hanwone
 * @date 2020-04-06 12:14:51
 **/
public interface SysMenuService extends IService<SysMenu> {

    /**
     * 根据角色ID获取菜单列表
     *
     * @param roleId
     * @return
     */
    List<SysMenu> findMenuByRoleId(String roleId);

    /**
     * 获取排序的菜单列表
     *
     * @return
     */
    List<SysMenu> getListBySortOk();

    /**
     * 获取最大的排序数
     *
     * @param pid
     * @return
     */
    Byte getMenuSortMax(String pid);

    /**
     * 根据菜单ID更新菜单状态
     *
     * @param statusEnum
     * @param ids
     * @return
     */
    boolean updateStatus(StatusEnum statusEnum, List<String> ids);

    /**
     * 根据菜单PID获取父级菜单列表
     *
     * @param pid
     * @param notId
     * @return
     */
    List<SysMenu> getListByPid(String pid, String notId);

    /**
     * 根据登录用户获取用户权限列表
     *
     * @param userId
     * @return
     */
    Set<String> getPermsByUserId(String userId);

    Set<String> getAllPerms();

    List<SysMenu> getListV2(SysMenu menu);

    void saveMenuV2(SysMenu menu);

    void delMenuV2(String id);

    Map<Integer, String> sortMenuV2(String pid, String notId);

    /**
     * @description: 获取用户目录列表
     * @author: HanHW
     * @date: 2023/10/18 16:17
     * @param: [userId:用户ID]
     * @return: java.util.Set<java.lang.String>
     **/
    Set<String> getDirsByUserIdV2(String userId);

    void freezeMenu(Map<String, Object> map);
}
