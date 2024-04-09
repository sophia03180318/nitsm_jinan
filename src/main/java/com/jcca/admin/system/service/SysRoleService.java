package com.jcca.admin.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jcca.admin.system.entity.SysMenu;
import com.jcca.admin.system.entity.SysRole;

import java.util.List;
import java.util.Map;

/**
 * @author hanwone
 * @date 2020-04-06 12:15:20
 **/
public interface SysRoleService extends IService<SysRole> {


    /**
     * 根据用户ID获取登录用户角色列表
     *
     * @param userId
     * @return
     */
    List<SysRole> findRoleByUserId(String userId);

    /**
     * 根据查看用户是否拥有角色
     *
     * @param userId
     * @return
     */
    boolean existsUserOk(String userId);

    /**
     * 根据角色名称查看角色是否重复
     *
     * @param role
     * @return
     */
    boolean repeatByName(SysRole role);

    /**
     * 获取最大排序数
     *
     * @param pid
     * @return
     */
    Byte getSortMax(String pid);

    /**
     * 获取所有的父级角色列表
     *
     * @param pid
     * @param notId
     * @return
     */
    List<SysRole> getListByPid(String pid, String notId);

    /**
     * 获取状态正常的角色列表
     *
     * @return
     */
    List<SysRole> getListBySortOk();

    /**
     * 根据角色ID更新角色状态
     *
     * @param param
     * @param ids
     * @return
     */
    boolean updateStatus(String param, List<String> ids);

    List<SysRole> getRoleListV2(SysRole role);

    void saveOrUpdateRoleV2(SysRole roleParam);

    void deleteRoleV2(String id);

    Map<Integer, String> sortListV2(String pid, String id);

    List<SysMenu> getRoleAuthV2(String id);

    List<SysRole> findUserRoleListV2(String id);
}
