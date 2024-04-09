package com.jcca.admin.system.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcca.admin.system.dao.SysUserMapper;
import com.jcca.admin.system.dao.SysUserRoleMapper;
import com.jcca.admin.system.entity.SysRole;
import com.jcca.admin.system.entity.SysUser;
import com.jcca.admin.system.entity.SysUserRole;
import com.jcca.admin.system.service.SysRoleService;
import com.jcca.admin.system.service.SysUserRoleService;
import com.jcca.admin.system.service.SysUserService;
import com.jcca.common.bean.constant.AdminConst;
import com.jcca.common.enums.ResultEnum;
import com.jcca.common.enums.StatusEnum;
import com.jcca.common.exception.ResultException;
import com.jcca.common.shiro.util.ShiroUtil;
import com.jcca.common.utils.EntityBeanUtil;
import com.jcca.common.utils.MyIdUtil;
import com.jcca.common.utils.StatusUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
 * @author hanwone
 * @date 2020-04-06 12:15
 **/
@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    @Resource
    private SysUserMapper sysUserMapper;
    @Resource
    private SysUserRoleMapper userRoleMapper;
    @Resource
    private SysUserRoleService userRoleService;
    @Resource
    private SysRoleService roleService;

    @Override
    public SysUser findByUsername(String username) {
        return sysUserMapper.findByUsername(username);
    }

    @Override
    public boolean repeatByUsername(SysUser user) {
        return sysUserMapper.findByUsername(user.getUsername()) != null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateStatus(String param, List<String> ids) {
        StatusEnum statusEnum = StatusUtil.getStatusEnum(param);
        if (ids != null) {
            ids.forEach(id -> {
                if (statusEnum == StatusEnum.DELETE) {
                    // 删除用户角色关联
                    QueryWrapper<SysUserRole> query = Wrappers.query();
                    query.eq("user_id", id);
                    userRoleMapper.delete(query);
                }

                SysUser user = new SysUser();
                user.setId(id);
                user.setStatus(statusEnum.getCode());
                sysUserMapper.updateById(user);
            });
        }

        return true;
    }

    /**
     * 根据组织ID获取所有管理该组织的用户
     *
     * @param orgId 组织ID
     * @return 用户列表
     */
    @Override
    public List<SysUser> findAllByOrgId(String orgId) {
        return sysUserMapper.findAllByOrgId(orgId);
    }

    @Override
    public List<SysUser> getUserListV2(SysUser user) {
        QueryWrapper<SysUser> wrapper = new QueryWrapper<>();
        if (StringUtils.isNotEmpty(user.getNickname())) {
            wrapper.like("nickname", user.getNickname());
        }
        if (StringUtils.isNotEmpty(user.getUsername())) {
            wrapper.like("username", user.getUsername());
        }

        if (user.getStatus() != null) {
            wrapper.eq("status", user.getStatus());
        }

        wrapper.ne("status", StatusEnum.DELETE.getCode());
        wrapper.orderByDesc("create_time");
        return this.list(wrapper);
    }

    @Override
    public void saveUserV2(SysUser user) {
        // 验证数据是否合格
        if (user.getId() == null) {

            // 判断密码是否为空
            if (user.getPassword().isEmpty() || "".equals(user.getPassword().trim())) {
                throw new ResultException(ResultEnum.USERNAME_PWD_NULL);
            }

            // 判断两次密码是否一致
            if (!user.getPassword().equals(user.getConfirm())) {
                throw new ResultException(ResultEnum.USER_INEQUALITY);
            }

            // 判断用户名是否重复
            if (this.repeatByUsername(user)) {
                throw new ResultException(ResultEnum.USER_EXIST);
            }

            // 对密码进行加密
            String salt = ShiroUtil.getRandomSalt();
            String encrypt = null;
            try {
                encrypt = ShiroUtil.encrypt(user.getPassword(), salt);
            } catch (UnsupportedEncodingException | NoSuchAlgorithmException e) {
                throw new ResultException(ResultEnum.PWD_ENCRYT_ERROR);
            }
            user.setPassword(encrypt);
            user.setPwdSalt(salt);
        }

        // 复制保留无需修改的数据
        if (user.getId() != null) {
            // 不允许操作超级管理员数据
            if (user.getId().equals(AdminConst.ADMIN_ID) &&
                    !ShiroUtil.getSubject().getId().equals(AdminConst.ADMIN_ID)) {
                throw new ResultException(ResultEnum.NO_ADMIN_AUTH);
            }

            // 判断用户名是否重复
            SysUser otherUser = this.findByUsername(user.getUsername());
            if (otherUser != null && !otherUser.getId().equals(user.getId())) {
                throw new ResultException(ResultEnum.USER_EXIST);
            }

            SysUser beUser = this.getById(user.getId());
            String[] fields = {"password", "pwdSalt", "picture"};
            EntityBeanUtil.copyProperties(beUser, user, fields);
        } else {
            user.setId(MyIdUtil.getId());
        }

        // 保存用户时候直接保存角色 20231020
        String id = user.getId();
        List<String> roleIdList = user.getRoleIdList();
        if (!CollectionUtils.isEmpty(roleIdList)) {
            // 不允许操作超级管理员数据
            if (AdminConst.ADMIN_ID.equals(id) &&
                    !ShiroUtil.getSubject().getId().equals(AdminConst.ADMIN_ID)) {
                throw new ResultException(ResultEnum.NO_ADMIN_AUTH);
            }

            // 更新用户角色
            userRoleService.updateUserRole(id, roleIdList);
        }

        // 保存数据
        user.setStatus(StatusEnum.OK.getCode());
        this.saveOrUpdate(user);
    }

    @Override
    public void editPasswordV2(String id, String password, String confirm) {

        // 判断密码是否为空
        if (password.isEmpty() || "".equals(password.trim())) {
            throw new ResultException(ResultEnum.USERNAME_PWD_NULL);
        }

        // 判断两次密码是否一致
        if (!password.equals(confirm)) {
            throw new ResultException(ResultEnum.USER_INEQUALITY);
        }

        // 不允许操作超级管理员数据
        if (id.equals(AdminConst.ADMIN_ID) &&
                !ShiroUtil.getSubject().getId().equals(AdminConst.ADMIN_ID)) {
            throw new ResultException(ResultEnum.NO_ADMIN_AUTH);
        }

        // 修改密码，对密码进行加密
        SysUser user = this.getById(id);
        String salt = ShiroUtil.getRandomSalt();
        String encrypt = null;
        try {
            encrypt = ShiroUtil.encrypt(password, salt);
        } catch (UnsupportedEncodingException | NoSuchAlgorithmException e) {
            throw new ResultException(ResultEnum.PWD_ENCRYT_ERROR);
        }
        user.setPassword(encrypt);
        user.setPwdSalt(salt);

        // 保存数据
        this.saveOrUpdate(user);
    }

    /**
     * @description: 查看用户详情
     * @author: HanHW
     * @date: 2023/10/20 15:44
     * @param: [id:用户ID]
     * @return: com.jcca.admin.system.entity.SysUser
     **/
    @Override
    public SysUser findDetailV2(String id) {
        SysUser one = this.getById(id);
        List<SysRole> roleList = roleService.findUserRoleListV2(id);
        one.setRoleList(roleList);
        return one;
    }
}