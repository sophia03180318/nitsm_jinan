package com.jcca.admin.system.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcca.admin.system.dao.SysUserRoleMapper;
import com.jcca.admin.system.entity.SysUserRole;
import com.jcca.admin.system.service.SysUserRoleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author hanwone
 * @date 2020-04-09 17:38
 **/
@Service
public class SysUserRoleServiceImpl extends ServiceImpl<SysUserRoleMapper, SysUserRole> implements SysUserRoleService {


    @Resource
    private SysUserRoleMapper userRoleMapper;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public int updateUserRole(String userId, List<String> roleIds) {

        QueryWrapper<SysUserRole> wrapper = new QueryWrapper<>();
        SysUserRole userRole = new SysUserRole();
        userRole.setUserId(userId);
        wrapper.setEntity(userRole);
        userRoleMapper.delete(wrapper);

        if (roleIds != null) {
            roleIds.forEach(roleId -> {
                SysUserRole ur = new SysUserRole();
                ur.setUserId(userId);
                ur.setRoleId(roleId);
                userRoleMapper.insert(ur);
            });
            return roleIds.size();
        }

        return -1;
    }
}