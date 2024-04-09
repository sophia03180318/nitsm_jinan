package com.jcca.admin.system.dao;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jcca.admin.system.entity.SysUser;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author hanwone
 * @date 2020-04-06 12:15:45
 **/
public interface SysUserMapper extends BaseMapper<SysUser> {


    /**
     * 通过用户名查找用户
     *
     * @param username
     * @return
     */
    SysUser findByUsername(@Param("username") String username);

    /**
     * 根据组织ID获取所有管理该组织的用户
     *
     * @param orgId 组织ID
     * @return 用户列表
     */
    List<SysUser> findAllByOrgId(String orgId);
}
