package com.jcca.admin.system.controller;

import cn.hutool.core.util.StrUtil;
import com.jcca.admin.system.entity.SysMenu;
import com.jcca.admin.system.service.SysMenuService;
import com.jcca.admin.system.validator.MenuValid;
import com.jcca.common.bean.ResultVo;
import com.jcca.common.enums.StatusEnum;
import com.jcca.common.log.annotation.ActionLog;
import com.jcca.common.log.constant.LogTypeConstant;
import com.jcca.common.utils.HttpServletUtil;
import com.jcca.common.utils.ResultVoUtil;
import com.jcca.common.utils.StatusUtil;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * @author hanwone
 * @date 2018/8/14
 */
@Controller
@RequestMapping("/system/menu")
public class MenuController {

    @Resource
    private SysMenuService menuService;

    /**
     * 跳转到列表页面
     */
    @GetMapping("/index")
    @RequiresPermissions("system:menu:index")
    public String index(Model model) {
        String search = HttpServletUtil.getRequest().getQueryString();
        model.addAttribute("search", search);
        return "/system/menu/index";
    }

    /**
     * 菜单数据列表
     */
    @GetMapping("/list")
    @RequiresPermissions("system:menu:index")
    @ResponseBody
    @ActionLog(name = "查看菜单列表", title = "菜单管理", key = LogTypeConstant.QUERY)
    @ApiOperation(value = "查看菜单列表")
    public ResultVo list(SysMenu menu) {

        return ResultVoUtil.success(menuService.getListV2(menu));
    }

    /**
     * 获取排序菜单列表
     */
    @GetMapping("/sortList/{pid}/{notId}")
    @RequiresPermissions({"system:menu:add", "system:menu:edit"})
    @ResponseBody
    public Map<Integer, String> sortList(
            @PathVariable(value = "pid", required = false) String pid,
            @PathVariable(value = "notId", required = false) String notId) {

        return menuService.sortMenuV2(pid, notId);
    }

    /**
     * 跳转到添加页面
     */
    @GetMapping({"/add", "/add/{pid}"})
    @RequiresPermissions("system:menu:add")
    public String toAdd(@PathVariable(value = "pid", required = false) String pid, Model model) {
        if (StrUtil.isNotEmpty(pid)) {
            model.addAttribute("pMenu", menuService.getById(pid));
        }
        return "/system/menu/add";
    }

    /**
     * 跳转到编辑页面
     */
    @GetMapping("/edit/{id}")
    @RequiresPermissions("system:menu:edit")
    public String toEdit(@PathVariable("id") String id, Model model) {
        SysMenu menu = menuService.getById(id);
        model.addAttribute("menu", menu);
        model.addAttribute("pMenu", menuService.getById(menu.getPid()));
        return "/system/menu/add";
    }

    /**
     * 保存添加/修改的数据
     *
     * @param valid 验证对象
     * @param menu  实体对象
     */
    @PostMapping("/save")
    @RequiresPermissions({"system:menu:add", "system:menu:edit"})
    @ResponseBody
    @Transactional(rollbackFor = Exception.class)
    @ActionLog(name = "新增或修改菜单", title = "菜单管理", key = LogTypeConstant.MODIFY)
    @ApiOperation(value = "保存菜单")
    public ResultVo save(SysMenu menu, @Validated MenuValid valid) {
        // 保存数据
        menuService.saveMenuV2(menu);

        return ResultVoUtil.SAVE_SUCCESS;
    }

    /**
     * 跳转到详细页面
     */
    @GetMapping("/detail/{id}")
    @RequiresPermissions("system:menu:detail")
    public String toDetail(@PathVariable("id") String id, Model model) {
        model.addAttribute("menu", menuService.getById(id));
        return "/system/menu/detail";
    }

    /**
     * 设置一条或者多条数据的状态
     */
    @RequestMapping("/status/{param}")
    @RequiresPermissions("system:menu:status")
    @ResponseBody
    @ActionLog(name = "设置菜单状态", title = "菜单管理", key = LogTypeConstant.MODIFY)
    public ResultVo status(
            @PathVariable("param") String param,
            @RequestParam(value = "ids", required = false) List<String> ids) {
        // 判断要删除的节点有无孩子节点
        for (int i = 0; i < ids.size(); i++) {
            List<SysMenu> children = menuService.getListByPid(ids.get(i), ids.get(i));
            if (children.size() > 0) {
                return ResultVoUtil.error("有子节点的节点不允许删除");
            }
        }

        // 更新状态
        StatusEnum statusEnum = StatusUtil.getStatusEnum(param);
        if (menuService.updateStatus(statusEnum, ids)) {
            return ResultVoUtil.success(statusEnum.getMessage() + "成功");
        } else {
            return ResultVoUtil.error(statusEnum.getMessage() + "失败，请重新操作");
        }
    }


}
