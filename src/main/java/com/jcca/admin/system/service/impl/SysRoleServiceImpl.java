package com.jcca.admin.system.service.impl;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcca.admin.system.dao.SysRoleMapper;
import com.jcca.admin.system.dao.SysRoleMenuMapper;
import com.jcca.admin.system.dao.SysUserRoleMapper;
import com.jcca.admin.system.entity.SysMenu;
import com.jcca.admin.system.entity.SysRole;
import com.jcca.admin.system.entity.SysRoleMenu;
import com.jcca.admin.system.entity.SysUserRole;
import com.jcca.admin.system.service.SysMenuService;
import com.jcca.admin.system.service.SysRoleService;
import com.jcca.common.bean.constant.AdminConst;
import com.jcca.common.enums.ResultEnum;
import com.jcca.common.enums.StatusEnum;
import com.jcca.common.exception.ResultException;
import com.jcca.common.shiro.util.ShiroUtil;
import com.jcca.common.utils.StatusUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.*;

/**
 * @author hanwone
 * @date 2020-04-06 12:15
 **/
@Service
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements SysRoleService {

    @Resource
    private SysRoleMapper sysRoleMapper;
    @Resource
    private SysUserRoleMapper userRoleMapper;
    @Resource
    private SysRoleMenuMapper roleMenuMapper;
    @Resource
    private SysMenuService menuService;

    @Override
    public List<SysRole> findRoleByUserId(String userId) {

        return sysRoleMapper.findRoleByUserId(userId);
    }

    @Override
    public boolean existsUserOk(String userId) {
        return sysRoleMapper.findRoleByUserId(userId) != null;
    }

    @Override
    public boolean repeatByName(SysRole role) {
        QueryWrapper<SysRole> wrapper = Wrappers.query();
        wrapper.eq("name", role.getName());
        wrapper.eq("status", StatusEnum.OK.getCode());
        return sysRoleMapper.selectOne(wrapper) != null;
    }

    @Override
    public Byte getSortMax(String pid) {
        return sysRoleMapper.getSortMax(pid);
    }

    @Override
    public List<SysRole> getListByPid(String pid, String notId) {
        QueryWrapper<SysRole> wrapper = Wrappers.query();
        wrapper.eq("pid", pid);
        wrapper.ne("id", notId);
        wrapper.eq("status", StatusEnum.OK.getCode());
        wrapper.orderByAsc("sort");
        return sysRoleMapper.selectList(wrapper);
    }

    @Override
    public List<SysRole> getListBySortOk() {
        QueryWrapper<SysRole> wrapper = Wrappers.query();
        wrapper.eq("status", StatusEnum.OK.getCode());
        wrapper.orderByAsc("sort");
        return sysRoleMapper.selectList(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateStatus(String param, List<String> ids) {
        StatusEnum statusEnum = StatusUtil.getStatusEnum(param);
        if (statusEnum == StatusEnum.DELETE) {
            ids.forEach(id -> {
                // 删除角色用户关联关系
                QueryWrapper<SysUserRole> userRole = Wrappers.query();
                userRole.eq("role_id", id);
                userRoleMapper.delete(userRole);

                // 删除角色菜单关联关系
                QueryWrapper<SysRoleMenu> roleMenu = Wrappers.query();
                roleMenu.eq("role_id", id);
                roleMenuMapper.delete(roleMenu);
            });
        }

        ids.forEach(id -> {
            SysRole r = new SysRole();
            r.setId(id);
            r.setStatus(statusEnum.getCode());
            sysRoleMapper.updateById(r);
        });
        return true;
    }

    @Override
    public List<SysRole> getRoleListV2(SysRole role) {
        QueryWrapper<SysRole> wrapper = new QueryWrapper<>();
        if (role.getTitle() != null) {
            wrapper.like("title", role.getTitle().trim());
        }
        wrapper.ne("status", StatusEnum.DELETE.getCode());
        wrapper.orderByAsc("sort");

        return this.list(wrapper);
    }

    @Override
    public void saveOrUpdateRoleV2(SysRole roleParam) {
        // 不允许操作根角色数据
        if (StrUtil.isNotEmpty(roleParam.getId()) && roleParam.getId().equals(AdminConst.ADMIN_ROLE_ID) &&
                !ShiroUtil.getSubject().getId().equals(AdminConst.ADMIN_ID)) {
            throw new ResultException(ResultEnum.NO_ADMINROLE_AUTH);
        }
        String pid = roleParam.getPid();
        String id = roleParam.getId();
        if (!StringUtils.isEmpty(pid) && !StringUtils.isEmpty(id) && Objects.equals(id, pid)) {
            throw new ResultException(ResultEnum.ID_PID_EQUAL);
        }

        // 判断角色编号是否重复
        if (StrUtil.isEmpty(roleParam.getId())) {
            if (this.repeatByName(roleParam)) {
                throw new ResultException(ResultEnum.ROLE_EXIST);
            }
        }

        if (StrUtil.isEmpty(roleParam.getId())) {
            // 排序为空时，添加到最后
            if (roleParam.getSort() == null) {
                Byte sortMax = this.getSortMax(pid);
                roleParam.setSort(sortMax != null ? (byte) (sortMax - 1) : 0);
            }
        }

        // 添加/更新全部上级序号
        if (!"0".equals(pid)) {
            SysRole pOrg = this.getById(pid);
            roleParam.setPids(pOrg.getPids() + ",[" + pid + "]");
        } else {
            roleParam.setPids("[0]");
        }

        // 排序功能
        String notId = roleParam.getId() != null ? roleParam.getId() : "0";
        List<SysRole> levelOrg = this.getListByPid(pid, notId);
        roleParam.setStatus(StatusEnum.OK.getCode());
        if (levelOrg.size() == 0) {
            roleParam.setSort((byte) 0);
        }
        levelOrg.add(roleParam.getSort(), roleParam);
        for (int i = 1; i <= levelOrg.size(); i++) {
            levelOrg.get(i - 1).setSort((byte) i);
        }

        // 保存数据
        this.saveOrUpdateBatch(levelOrg);
    }

    @Override
    public void deleteRoleV2(String id) {
        if (StringUtils.isEmpty(id)) {
            throw new ResultException(ResultEnum.PARAM_ERROR);
        }

        // 不能修改超级管理员角色状态
        if (AdminConst.ADMIN_ROLE_ID.equals(id)) {
            throw new ResultException(ResultEnum.NO_ADMINROLE_AUTH);
        }

        // 判断要删除的节点有无孩子节点
        SysRole one = this.getById(id);
        if (Objects.isNull(one)) {
            throw new ResultException(ResultEnum.CANNOT_FIND);
        }
        List<SysRole> children = this.getListByPid(one.getId(), id);
        if (children.size() > 0) {
            throw new ResultException(ResultEnum.DATA_DELETE);
        }

        // 更新状态
        if (!this.updateStatus(StatusEnum.DELETE.name(), Collections.singletonList(id))) {
            throw new ResultException(ResultEnum.UPDATE_FAIL);
        }
    }

    @Override
    public Map<Integer, String> sortListV2(String pid, String id) {
        // 本级排序组织列表
        id = id != null ? id : "0";
        List<SysRole> levelOrg = this.getListByPid(pid, id);
        Map<Integer, String> sortMap = new TreeMap<>();
        for (int i = 1; i <= levelOrg.size(); i++) {
            sortMap.put(i, levelOrg.get(i - 1).getTitle());
        }
        return sortMap;
    }

    @Override
    public List<SysMenu> getRoleAuthV2(String id) {
        // 获取指定角色权限资源
        List<SysMenu> menuList = menuService.findMenuByRoleId(id);
        // 获取全部菜单列表
        List<SysMenu> list = menuService.getListBySortOk();
        // 融合两项数据
        List<SysMenu> resultList = new ArrayList<>();
        for (SysMenu menu : list) {
            SysMenu m = new SysMenu();
            BeanUtils.copyProperties(menu, m);
            for (SysMenu auth : menuList) {
                m.setRemark("");
                if (menu.getId().equals(auth.getId())) {
                    m.setRemark("auth:true");
                    break;
                }
            }
            resultList.add(m);
        }

        return resultList;
    }

    @Override
    public List<SysRole> findUserRoleListV2(String id) {
        // 获取指定用户角色资源
        List<SysRole> roleList = this.findRoleByUserId(id);
        // 获取全部角色列表
        List<SysRole> list = this.getListBySortOk();
        // 融合两项数据
        List<SysRole> resultList = new ArrayList<>();
        for (SysRole role : list) {
            SysRole r = new SysRole();
            BeanUtils.copyProperties(role, r);
            for (SysRole auth : roleList) {
                r.setRemark("");
                if (role.getId().equals(auth.getId())) {
                    r.setRemark("auth:true");
                    break;
                }
            }
            resultList.add(r);
        }
        return resultList;
    }
}