package com.jcca.web2.controller;

import com.jcca.admin.system.entity.SysMenu;
import com.jcca.admin.system.entity.SysOrg;
import com.jcca.admin.system.entity.SysRole;
import com.jcca.admin.system.service.SysRoleMenuService;
import com.jcca.admin.system.service.SysRoleOrgService;
import com.jcca.admin.system.service.SysRoleService;
import com.jcca.common.bean.ResultVo;
import com.jcca.common.bean.constant.AdminConst;
import com.jcca.common.enums.ResultEnum;
import com.jcca.common.exception.ResultException;
import com.jcca.common.log.annotation.ActionLog;
import com.jcca.common.log.constant.LogTypeConstant;
import com.jcca.common.shiro.util.ShiroUtil;
import com.jcca.common.utils.ResultVoUtil;
import com.jcca.web2.dto.AuthMenuDto;
import com.jcca.web2.dto.AuthOrgDto;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author HanHW
 * @description 角色管理V2
 * @className RoleControllerV2
 * @date 2023/10/19 11:48
 * @since 2.1.0.0
 */
@RestController
@RequestMapping("/api/v2/role")
@Api(tags = "角色管理V2")
public class RoleControllerV2 {

    @Resource
    private SysRoleService roleService;
    @Resource
    private SysRoleMenuService roleMenuService;
    @Resource
    private SysRoleOrgService roleOrgService;

    @PostMapping("/list")
    @ApiOperation("获取角色列表")
    public ResultVo<Object> getList(@RequestBody SysRole role) {

        return ResultVoUtil.success(roleService.getRoleListV2(role));
    }

    @PostMapping("/save")
    @ApiOperation("保存角色信息")
    @RequiresPermissions("api:v2:role:save")
    @ActionLog(name = "新增或修改角色信息", title = "角色管理", key = LogTypeConstant.ADD)
    public ResultVo<Object> save(@RequestBody @Validated SysRole roleParam) {

        roleService.saveOrUpdateRoleV2(roleParam);

        return ResultVoUtil.success();
    }

    @PostMapping("/delete/{id}")
    @ApiOperation("删除角色信息")
    @RequiresPermissions("api:v2:role:delete")
    @ActionLog(name = "修改角色状态", title = "角色管理", key = LogTypeConstant.MODIFY)
    public ResultVo<Object> delete(@PathVariable String id) {

        roleService.deleteRoleV2(id);

        return ResultVoUtil.success();
    }

    @GetMapping("/sortList/{pid}/{id}")
    @RequiresPermissions("api:v2:role:save")
    @ApiOperation("角色排序")
    public ResultVo<Object> sortList(
            @PathVariable(value = "pid") String pid,
            @PathVariable(value = "id") String id) {

        return ResultVoUtil.success(roleService.sortListV2(pid, id));
    }

    @GetMapping("/authList/{id}")
    @RequiresPermissions("api:v2:role:authList")
    @ApiOperation("角色下菜单列表")
    public ResultVo<Object> authList(@PathVariable(value = "id") String id) {

        List<SysMenu> list = roleService.getRoleAuthV2(id);

        return ResultVoUtil.success(list);
    }

    @PostMapping("/authMenu")
    @RequiresPermissions("api:v2:role:authMenu")
    @ApiOperation("保存角色菜单信息")
    @ActionLog(name = "保存角色菜单信息", title = "角色管理", key = LogTypeConstant.ADD)
    public ResultVo<Object> authMenu(@RequestBody @Validated AuthMenuDto dto) {
        String id = dto.getId();
        List<String> menuIds = dto.getMenuIds();

        // 不允许操作管理员角色数据
        if (AdminConst.ADMIN_ROLE_ID.equals(id) &&
                !ShiroUtil.getSubject().getId().equals(AdminConst.ADMIN_ID)) {
            throw new ResultException(ResultEnum.NO_ADMINROLE_AUTH);
        }

        // 更新角色菜单
        roleMenuService.saveMenus(id, menuIds);

        return ResultVoUtil.success();
    }

    @PostMapping("/authOrg")
    @RequiresPermissions("api:v2:role:authOrg")
    @ApiOperation("给角色分配组织")
    @ActionLog(name = "给角色分配组织", title = "角色管理", key = LogTypeConstant.MODIFY)
    public ResultVo<Object> authOrg(@RequestBody @Validated AuthOrgDto dto) {

        String id = dto.getId();
        List<String> orgIds = dto.getOrgIds();

        // 不允许操作超级管理员数据
        if (AdminConst.ADMIN_ID.equals(id) &&
                !ShiroUtil.getSubject().getId().equals(AdminConst.ADMIN_ID)) {
            throw new ResultException(ResultEnum.NO_ADMIN_AUTH);
        }

        // 更新角色组织
        roleOrgService.updateRoleOrg(id, orgIds);

        return ResultVoUtil.success();
    }

    @GetMapping("/orgList/{id}")
    @RequiresPermissions("api:v2:role:orgList")
    @ApiOperation("角色下组织列表")
    public ResultVo<Object> orgList(@PathVariable(value = "id") String id) {

        List<SysOrg> resultList = roleOrgService.getRoleOrgListV2(id);

        return ResultVoUtil.success(resultList);
    }

}
