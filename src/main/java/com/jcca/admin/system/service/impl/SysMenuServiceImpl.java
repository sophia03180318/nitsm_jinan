package com.jcca.admin.system.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcca.admin.system.dao.SysMenuMapper;
import com.jcca.admin.system.dao.SysRoleMenuMapper;
import com.jcca.admin.system.entity.SysMenu;
import com.jcca.admin.system.entity.SysRoleMenu;
import com.jcca.admin.system.service.SysMenuService;
import com.jcca.common.config.thymeleaf.utility.DictUtil;
import com.jcca.common.enums.ResultEnum;
import com.jcca.common.enums.StatusEnum;
import com.jcca.common.exception.ResultException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;

/**
 * @author hanwone
 * @date 2020-04-06 12:14
 **/
@Service
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu> implements SysMenuService {

    @Resource
    private SysMenuMapper menuMapper;

    @Resource
    private SysRoleMenuMapper roleMenuMapper;

    @Override
    public List<SysMenu> findMenuByRoleId(String roleId) {
        return menuMapper.findMenuByRoleId(roleId);
    }

    @Override
    public List<SysMenu> getListBySortOk() {
        SysMenu menu = new SysMenu();
        menu.setStatus(StatusEnum.OK.getCode());
        QueryWrapper<SysMenu> wrapper = new QueryWrapper<>();
        wrapper.orderByAsc("sort", "type");
        wrapper.setEntity(menu);

        return menuMapper.selectList(wrapper);
    }

    @Override
    public Byte getMenuSortMax(String pid) {
        return menuMapper.getMenuSortMax(pid);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateStatus(StatusEnum statusEnum, List<String> ids) {
        // 获取与之关联的所有菜单
        Set<SysMenu> treeMenus = new HashSet<>();
        List<SysMenu> menus = menuMapper.selectBatchIds(ids);
        menus.forEach(menu -> {
            treeMenus.add(menu);

            QueryWrapper<SysMenu> wrapper = new QueryWrapper<>();
            wrapper.eq("pid", menu.getId());
            treeMenus.addAll(menuMapper.selectList(wrapper));
        });

        treeMenus.forEach(menu -> {
            // 删除角色菜单关联关系
            if (statusEnum == StatusEnum.DELETE) {
                QueryWrapper<SysRoleMenu> wrapper = new QueryWrapper<>();
                wrapper.eq("menu_id", menu.getId());
                roleMenuMapper.delete(wrapper);
            }
            // 更新关联的所有菜单状态
            menu.setStatus(statusEnum.getCode());
            menuMapper.updateById(menu);
        });

        return !treeMenus.isEmpty();
    }

    @Override
    public List<SysMenu> getListByPid(String pid, String notId) {
        QueryWrapper<SysMenu> wrapper = new QueryWrapper<>();
        wrapper.eq("pid", pid);
        wrapper.in("status", Arrays.asList(StatusEnum.OK.getCode(), StatusEnum.FREEZED.getCode()));
        wrapper.ne("id", notId);
        return menuMapper.selectList(wrapper);
    }

    /**
     * 根据登录用户获取用户权限列表
     *
     * @param userId
     * @return
     */
    @Override
    public Set<String> getPermsByUserId(String userId) {
        return menuMapper.getPermsByUserId(userId);
    }

    @Override
    public Set<String> getAllPerms() {
        return menuMapper.getAllPerms();
    }

    @Override
    public List<SysMenu> getListV2(SysMenu menu) {
        QueryWrapper<SysMenu> wrapper = new QueryWrapper<>();

        if (menu.getStatus() == null) {
            wrapper.in("status", Arrays.asList(StatusEnum.OK.getCode(), StatusEnum.FREEZED.getCode()));
        } else {
            wrapper.eq("status", menu.getStatus());
        }
        if (StringUtils.isNotEmpty(menu.getTitle())) {
            wrapper.like("title", menu.getTitle());
        }
        if (StringUtils.isNotEmpty(menu.getUrl())) {
            wrapper.like("url", menu.getUrl());
        }

        wrapper.orderByAsc("sort");
        List<SysMenu> list = this.list(wrapper);

        list.forEach(editMenu -> {
            String type = String.valueOf(editMenu.getType());
            editMenu.setTypeStr(DictUtil.keyValue("MENU_TYPE", type));
        });
        return list;
    }

    @Override
    public void saveMenuV2(SysMenu menu) {
        String id = menu.getId();
        String pid = menu.getPid();
        if (!StringUtils.isEmpty(pid) && !StringUtils.isEmpty(id) && Objects.equals(id, pid)) {
            throw new ResultException(ResultEnum.ID_PID_EQUAL);
        }

        if (id == null) {
            // 排序为空时，添加到最后
            if (menu.getSort() == null) {
                Byte sortMax = this.getMenuSortMax(pid);
                menu.setSort(sortMax != null ? (byte) (sortMax - (byte) 1) : (byte) 0);
            }
        }

        // 添加/更新全部上级序号
        if ("0".equals(pid)) {
            menu.setPids("[0]");
        } else {
            SysMenu pMenu = this.getById(pid);
            menu.setPids(pMenu.getPids() + ",[" + pid + "]");
        }
        menu.setStatus(StatusEnum.OK.getCode());

        // 排序功能
        Byte sort = menu.getSort();
        String notId = id != null ? id : "0";

        QueryWrapper<SysMenu> wrapper = new QueryWrapper<>();
        wrapper.eq("pid", pid);
        wrapper.ne("id", notId);
        wrapper.in("status", Arrays.asList(StatusEnum.OK.getCode(), StatusEnum.FREEZED.getCode()));
        wrapper.orderByAsc("sort");
        List<SysMenu> levelMenu = this.list(wrapper);

        levelMenu.add(sort, menu);
        for (int i = 1; i <= levelMenu.size(); i++) {
            levelMenu.get(i - 1).setSort((byte) i);
        }

        // 保存数据
        this.saveOrUpdateBatch(levelMenu);
    }

    @Override
    public void delMenuV2(String id) {
        if (StringUtils.isEmpty(id)) {
            throw new ResultException(ResultEnum.PARAM_ERROR);
        }
        SysMenu one = this.getById(id);
        if (Objects.isNull(one)) {
            throw new ResultException(ResultEnum.CANNOT_FIND);
        }

        List<SysMenu> children = this.getListByPid(one.getId(), one.getId());
        if (!children.isEmpty()) {
            throw new ResultException(ResultEnum.DATA_DELETE);
        }
        one.setStatus(StatusEnum.DELETE.getCode());
        this.updateById(one);
    }

    @Override
    public Map<Integer, String> sortMenuV2(String pid, String notId) {
        // 本级排序菜单列表
        notId = notId != null ? notId : "0";

        QueryWrapper<SysMenu> wrapper = new QueryWrapper<>();
        wrapper.eq("pid", pid);
        wrapper.in("status", Arrays.asList(StatusEnum.OK.getCode(), StatusEnum.FREEZED.getCode()));
        wrapper.ne("id", notId);
        wrapper.orderByAsc("sort");
        List<SysMenu> levelMenu = this.list(wrapper);

        Map<Integer, String> sortMap = new TreeMap<>();
        for (int i = 1; i <= levelMenu.size(); i++) {
            sortMap.put(i, levelMenu.get(i - 1).getTitle());
        }
        return sortMap;
    }

    /**
     * @param userId
     * @description: 获取用户目录列表
     * @author: HanHW
     * @date: 2023/10/18 16:17
     * @param: [userId:用户ID]
     * @return: java.util.Set<java.lang.String>
     */
    @Override
    public Set<String> getDirsByUserIdV2(String userId) {

        return menuMapper.getDirsByUserId(userId);
    }

    @Override
    public void freezeMenu(Map<String, Object> map) {
        Object id = map.get("id");
        Object status = map.get("status");
        if (Objects.isNull(id) || Objects.isNull(status)) {
            throw new ResultException(ResultEnum.PARAM_ERROR);
        }
        SysMenu one = this.getById(id.toString());
        if (Objects.isNull(one)) {
            throw new ResultException(ResultEnum.CANNOT_FIND);
        }

        one.setStatus(Byte.parseByte(status.toString()));
        this.updateById(one);

        List<SysMenu> children = this.getListByPid(one.getId(), one.getId());
        for (SysMenu child : children) {
            map.put("id", child.getId());
            map.put("status", status);
            this.freezeMenu(map);
        }
    }
}