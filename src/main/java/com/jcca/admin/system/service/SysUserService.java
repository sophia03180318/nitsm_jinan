package com.jcca.admin.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jcca.admin.system.entity.SysUser;

import java.util.List;

/**
 * @author hanwone
 * @date 2020-04-06 12:15:45
 **/
public interface SysUserService extends IService<SysUser> {


    /**
     * 通过用户名查找用户
     *
     * @param username
     * @return
     */
    SysUser findByUsername(String username);

    /**
     * 根据用户名判断用户是否存在
     *
     * @param user
     * @return
     */
    boolean repeatByUsername(SysUser user);

    /**
     * 更新用户状态
     *
     * @param param
     * @param ids
     * @return
     */
    boolean updateStatus(String param, List<String> ids);

    /**
     * 根据组织ID获取所有管理该组织的用户
     *
     * @param orgId 组织ID
     * @return 用户列表
     */
    List<SysUser> findAllByOrgId(String orgId);

    List<SysUser> getUserListV2(SysUser user);

    void saveUserV2(SysUser user);

    void editPasswordV2(String id, String pwd, String confirm);

    SysUser findDetailV2(String id);
}
