package com.jcca.admin.system.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.jcca.admin.system.entity.SysRole;
import com.jcca.admin.system.entity.SysUserRole;
import com.jcca.admin.system.service.*;
import com.jcca.admin.system.validator.RoleValid;
import com.jcca.common.bean.ResultVo;
import com.jcca.common.bean.constant.AdminConst;
import com.jcca.common.enums.ResultEnum;
import com.jcca.common.exception.ResultException;
import com.jcca.common.log.annotation.ActionLog;
import com.jcca.common.log.constant.LogTypeConstant;
import com.jcca.common.shiro.util.ShiroUtil;
import com.jcca.common.utils.HttpServletUtil;
import com.jcca.common.utils.ResultVoUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author hanwone
 * @date 2018/8/14
 */
@Controller
@RequestMapping("/system/role")
@Slf4j
public class RoleController {

    @Resource
    private SysRoleService roleService;
    @Resource
    private SysRoleMenuService roleMenuService;
    @Resource
    private SysUserRoleService userRoleService;
    @Resource
    private SysUserService userService;
    @Resource
    private SysRoleOrgService roleOrgService;

    /**
     * 跳转到列表页面
     */
    @GetMapping("/index")
    @RequiresPermissions("system:role:index")
    public String index(Model model) {
        String search = HttpServletUtil.getRequest().getQueryString();
        model.addAttribute("search", search);
        return "/system/role/index";
    }

    /**
     * 列表页面
     */
    @GetMapping("/list")
    @RequiresPermissions(value = {"system:org:index", "system:user:index"}, logical = Logical.OR)
    @ResponseBody
    @ActionLog(name = "查看角色列表", title = "角色管理", key = LogTypeConstant.QUERY)
    public ResultVo list(SysRole role) {

        return ResultVoUtil.success(roleService.getRoleListV2(role));
    }

    /**
     * 获取排序组织列表
     */
    @GetMapping("/sortList/{pid}/{notId}")
    @RequiresPermissions({"system:role:add", "system:role:edit"})
    @ResponseBody
    public Map<Integer, String> sortList(
            @PathVariable(value = "pid", required = false) String pid,
            @PathVariable(value = "notId", required = false) String notId) {

        return roleService.sortListV2(pid, notId);
    }

    /**
     * 跳转到添加页面
     */
    @GetMapping({"/add", "/add/{pid}"})
    @RequiresPermissions("system:role:add")
    public String toAdd(@PathVariable(value = "pid", required = false) String pid, Model model) {
        if (StrUtil.isNotEmpty(pid)) {
            model.addAttribute("pRole", roleService.getById(pid));
        }

        return "/system/role/add";
    }

    /**
     * 跳转到编辑页面
     */
    @GetMapping("/edit/{id}")
    @RequiresPermissions("system:role:edit")
    public String toEdit(@PathVariable("id") String id, Model model) {
        SysRole role = roleService.getById(id);
        SysRole pRole = roleService.getById(role.getPid());
        if (pRole == null) {
            pRole = new SysRole();
            pRole.setId("0");
            pRole.setTitle("顶级");
        }

        model.addAttribute("role", role);
        model.addAttribute("pRole", pRole);
        return "/system/role/add";
    }

    /**
     * 保存添加/修改的数据
     *
     * @param valid     验证对象
     * @param roleParam 实体对象
     */
    @PostMapping("/save")
    @RequiresPermissions({"system:role:add", "system:role:edit"})
    @ResponseBody
    @ActionLog(name = "新增或修改角色信息", title = "角色管理", key = LogTypeConstant.MODIFY)
    public ResultVo save(@Validated RoleValid valid, SysRole roleParam) {

        roleService.saveOrUpdateRoleV2(roleParam);

        return ResultVoUtil.SAVE_SUCCESS;
    }

    /**
     * 跳转到授权页面
     */
    @GetMapping("/auth/{id}")
    @RequiresPermissions("system:role:auth")
    public String toAuth(@PathVariable(value = "id") String id, Model model) {
        model.addAttribute("id", id);
        return "/system/role/auth";
    }

    /**
     * 获取权限资源列表
     */
    @GetMapping("/authList")
    @RequiresPermissions("system:role:auth")
    @ResponseBody
    public ResultVo authList(@RequestParam(value = "ids") String ids) {

        return ResultVoUtil.success(roleService.getRoleAuthV2(ids));
    }

    /**
     * 保存授权信息
     */
    @PostMapping("/auth")
    @RequiresPermissions("system:role:auth")
    @ResponseBody
    @ActionLog(name = "保存角色授权信息", title = "角色管理", key = LogTypeConstant.MODIFY)
    public ResultVo auth(
            @RequestParam(value = "id", required = true) String id,
            @RequestParam(value = "authId", required = false) List<String> menuIds) {
        // 不允许操作管理员角色数据
        if (AdminConst.ADMIN_ROLE_ID.equals(id) &&
                !ShiroUtil.getSubject().getId().equals(AdminConst.ADMIN_ID)) {
            throw new ResultException(ResultEnum.NO_ADMINROLE_AUTH);
        }

        // 更新角色菜单
        roleMenuService.saveMenus(id, menuIds);

        return ResultVoUtil.SAVE_SUCCESS;
    }

    /**
     * 跳转到详细页面
     */
    @GetMapping("/detail/{id}")
    public String toDetail(@PathVariable("id") String id, Model model) {
        model.addAttribute("role", roleService.getById(id));
        return "/system/role/detail";
    }

    /**
     * 跳转到拥有该角色的用户列表页面
     */
    @GetMapping("/userList/{id}")
    @RequiresPermissions("system:role:detail")
    public String toUserList(@PathVariable("id") String id, Model model) {
        QueryWrapper<SysUserRole> userRoleQueryWrapper = Wrappers.query();
        userRoleQueryWrapper.eq("role_id", id);
        List<SysUserRole> userRoleList = userRoleService.list(userRoleQueryWrapper);

        List<String> userIds = new ArrayList<>();
        userRoleList.forEach(ur -> {
            userIds.add(ur.getUserId());
        });

        model.addAttribute("list", Collections.emptyList());
        if (userIds.size() > 0) {
            model.addAttribute("list", userService.listByIds(userIds));
        }
        return "/system/role/userList";
    }

    /**
     * 设置一条或者多条数据的状态
     */
    @RequestMapping("/status/{param}")
    @RequiresPermissions("system:role:status")
    @ResponseBody
    @ActionLog(name = "修改角色状态", title = "角色管理", key = LogTypeConstant.MODIFY)
    public ResultVo status(
            @PathVariable("param") String param,
            @RequestParam(value = "ids", required = false) List<String> ids) {
        // 不能修改超级管理员角色状态
        if (ids.contains(AdminConst.ADMIN_ROLE_ID)) {
            throw new ResultException(ResultEnum.NO_ADMINROLE_AUTH);
        }

        // 判断要删除的节点有无孩子节点
        for (int i = 0; i < ids.size(); i++) {
            List<SysRole> children = roleService.getListByPid(ids.get(i), ids.get(i));
            if (children.size() > 0) {
                return ResultVoUtil.error("有子节点的节点不允许删除");
            }
        }

        // 更新状态
        if (roleService.updateStatus(param, ids)) {
            return ResultVoUtil.success("设置成功");
        } else {
            return ResultVoUtil.error("设置失败，请重新操作");
        }
    }

    /**
     * 跳转到组织分配页面
     */
    @GetMapping("/org/{id}")
    @RequiresPermissions("system:role:org")
    public String toRole(@PathVariable(value = "id") String ids, Model model) {
        model.addAttribute("id", ids);
        return "/system/role/org";
    }

    /**
     * 保存角色组织分配信息
     */
    @PostMapping("/authOrg")
    @RequiresPermissions("system:role:org")
    @ResponseBody
    @ActionLog(name = "给角色分配组织", title = "角色管理", key = LogTypeConstant.MODIFY)
    public ResultVo authOrg(
            @RequestParam(value = "id", required = true) String id,
            @RequestParam(value = "authId", required = false) List<String> orgIds) {

        // 不允许操作超级管理员数据
        if (AdminConst.ADMIN_ID.equals(id) &&
                !ShiroUtil.getSubject().getId().equals(AdminConst.ADMIN_ID)) {
            throw new ResultException(ResultEnum.NO_ADMIN_AUTH);
        }

        // 更新角色组织
        roleOrgService.updateRoleOrg(id, orgIds);

        return ResultVoUtil.SAVE_SUCCESS;
    }

    /**
     * 获取角色组织列表
     */
    @GetMapping("/orgList")
    @RequiresPermissions("system:role:org")
    @ResponseBody
    public ResultVo orgList(@RequestParam(value = "ids") String ids) {
        return ResultVoUtil.success(roleOrgService.getRoleOrgListV2(ids));
    }
}
