package com.jcca.admin.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jcca.admin.system.entity.SysUserRole;

import java.util.List;

/**
 * @author hanwone
 * @date 2020-04-09 17:38:40
 **/
public interface SysUserRoleService extends IService<SysUserRole> {


    /**
     * 更新用户角色关系
     *
     * @param userId
     * @param roleIds
     * @return
     */
    int updateUserRole(String userId, List<String> roleIds);
}
