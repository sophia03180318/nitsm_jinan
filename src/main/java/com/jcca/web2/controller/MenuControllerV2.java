package com.jcca.web2.controller;

import com.jcca.admin.system.entity.SysMenu;
import com.jcca.admin.system.service.SysMenuService;
import com.jcca.common.bean.ResultVo;
import com.jcca.common.log.annotation.ActionLog;
import com.jcca.common.log.constant.LogTypeConstant;
import com.jcca.common.utils.ResultVoUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @author HanHW
 * @description 菜单管理V2
 * @className MenuControllerV2
 * @date 2023/10/18 13:04
 * @since 2.1.0.0
 */
@RestController
@RequestMapping("/api/v2/menu")
@Api(tags = "菜单管理V2")
public class MenuControllerV2 {

    @Resource
    private SysMenuService menuService;

    @PostMapping("/list")
    @ApiOperation("获取菜单列表")
    @RequiresPermissions("api:v2:menu:list")
    public ResultVo<Object> getList(@RequestBody SysMenu menu) {

        return ResultVoUtil.success(menuService.getListV2(menu));
    }

    @PostMapping("/save")
    @ApiOperation("保存菜单")
    @RequiresPermissions("api:v2:menu:save")
    @ActionLog(name = "保存菜单", title = "菜单管理", key = LogTypeConstant.ADD)
    public ResultVo<Object> saveMenu(@RequestBody @Validated SysMenu menu) {

        menuService.saveMenuV2(menu);

        return ResultVoUtil.success();
    }

    @PostMapping("/delete/{id}")
    @ApiOperation("删除菜单")
    @RequiresPermissions("api:v2:menu:delete")
    @ActionLog(name = "删除菜单", title = "菜单管理", key = LogTypeConstant.REMOVEE)
    public ResultVo<Object> delMenu(@PathVariable String id) {

        menuService.delMenuV2(id);

        return ResultVoUtil.success();
    }

    @PostMapping("/sortList/{pid}/{id}")
    @ApiOperation("菜单排序")
    @RequiresPermissions("api:v2:menu:save")
    public ResultVo<Object> sortList(@PathVariable(value = "pid") String pid,
                                     @PathVariable(value = "id") String notId) {

        return ResultVoUtil.success(menuService.sortMenuV2(pid, notId));
    }

}
