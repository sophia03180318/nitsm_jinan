package com.jcca.admin.system.controller;

import cn.hutool.core.io.resource.ClassPathResource;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.jcca.admin.system.entity.SysRole;
import com.jcca.admin.system.entity.SysUser;
import com.jcca.admin.system.service.SysRoleService;
import com.jcca.admin.system.service.SysUserRoleService;
import com.jcca.admin.system.service.SysUserService;
import com.jcca.admin.system.util.TemplateExportUtil;
import com.jcca.admin.system.validator.UserValid;
import com.jcca.common.bean.ResultVo;
import com.jcca.common.bean.constant.AdminConst;
import com.jcca.common.config.mybatisplus.PagePlugin;
import com.jcca.common.enums.ResultEnum;
import com.jcca.common.enums.StatusEnum;
import com.jcca.common.exception.ResultException;
import com.jcca.common.log.annotation.ActionLog;
import com.jcca.common.log.constant.LogTypeConstant;
import com.jcca.common.shiro.util.ShiroUtil;
import com.jcca.common.utils.ResultVoUtil;
import com.jcca.common.utils.SpringContextUtil;
import com.jcca.common.utils.file.config.properties.UploadProjectProperties;
import com.jcca.poi.xssf.streaming.SXSSFWorkbook;
import com.jcca.web.asset.utils.DispatchRecordExcelUtil;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.FileCopyUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

/**
 * @author hanwone
 * @date 2018/8/14
 */
@Controller
@RequestMapping("/system/user")
public class UserController {

    @Resource
    private SysUserService userService;

    @Resource
    private SysRoleService roleService;

    @Resource
    private SysUserRoleService userRoleService;

    /**
     * 列表页面
     */
    @GetMapping("/index")
    @RequiresPermissions("system:user:index")
    @ActionLog(name = "查看用户列表信息", title = "用户管理", key = LogTypeConstant.QUERY)
    public String index(Model model, SysUser user, Integer size, Integer page) {

        // 获取用户列表
        IPage iPage = PagePlugin.startPage(page, size);

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
        iPage = userService.page(iPage, wrapper);

        List<SysUser> records = iPage.getRecords();
        // 封装数据
        model.addAttribute("list", records);
        model.addAttribute("page", iPage);
        return "/system/user/index";
    }

    /**
     * 跳转到添加页面
     */
    @GetMapping("/add")
    @RequiresPermissions("system:user:add")
    public String toAdd() {
        return "/system/user/add";
    }

    /**
     * 跳转到编辑页面
     */
    @GetMapping("/edit/{id}")
    @RequiresPermissions("system:user:edit")
    public String toEdit(@PathVariable("id") String id, Model model) {
        model.addAttribute("user", userService.getById(id));
        return "/system/user/add";
    }

    /**
     * 保存添加/修改的数据
     *
     * @param valid 验证对象
     * @param user  实体对象
     */
    @PostMapping("/save")
    @RequiresPermissions({"system:user:add", "system:user:edit"})
    @ResponseBody
    @ActionLog(name = "新增或修改用户信息", title = "用户管理", key = LogTypeConstant.MODIFY)
    public ResultVo save(@Validated UserValid valid, SysUser user) {

        userService.saveUserV2(user);

        return ResultVoUtil.SAVE_SUCCESS;
    }

    /**
     * 跳转到详细页面
     */
    @GetMapping("/detail/{id}")
    public String toDetail(@PathVariable("id") String id, Model model) {
        model.addAttribute("user", userService.getById(id));
        return "/system/user/detail";
    }

    /**
     * 跳转到修改密码页面
     */
    @GetMapping("/pwd/{id}")
    @RequiresPermissions("system:user:pwd")
    public String toEditPassword(Model model, @PathVariable("id") String id) {
        model.addAttribute("id", id);
        return "/system/user/pwd";
    }

    /**
     * 修改密码
     */
    @PostMapping("/pwd/{id}")
    @RequiresPermissions("system:user:pwd")
    @ResponseBody
    @ActionLog(name = "修改用户密码", title = "用户管理", key = LogTypeConstant.MODIFY)
    public ResultVo editPassword(String password, String confirm, @PathVariable("id") String ids) {

        userService.editPasswordV2(ids, password, confirm);

        return ResultVoUtil.success("修改成功");
    }

    /**
     * 跳转到角色分配页面
     */
    @GetMapping("/role/{id}")
    @RequiresPermissions("system:user:role")
    public String toRole(@PathVariable(value = "id") String ids, Model model) {
        model.addAttribute("id", ids);
        return "/system/user/role";
    }

    /**
     * 角色树展示
     */
    @GetMapping("/roleList")
    @RequiresPermissions("system:user:role")
    @ResponseBody
    public ResultVo roleList(@RequestParam(value = "ids") String ids) {

        List<SysRole> resultList = roleService.findUserRoleListV2(ids);

        return ResultVoUtil.success(resultList);
    }

    /**
     * 保存角色分配信息
     */
    @PostMapping("/role")
    @RequiresPermissions("system:user:role")
    @ResponseBody
    @ActionLog(name = "修改用户角色", title = "用户管理", key = LogTypeConstant.QUERY)
    public ResultVo auth(
            @RequestParam(value = "id", required = true) String id,
            @RequestParam(value = "authId", required = false) List<String> roleIds) {

        // 不允许操作超级管理员数据
        if (AdminConst.ADMIN_ID.equals(id) &&
                !ShiroUtil.getSubject().getId().equals(AdminConst.ADMIN_ID)) {
            throw new ResultException(ResultEnum.NO_ADMIN_AUTH);
        }

        // 更新用户角色
        userRoleService.updateUserRole(id, roleIds);

        return ResultVoUtil.SAVE_SUCCESS;
    }

    /**
     * 获取用户头像
     */
    @GetMapping("/picture")
    public void picture(String p, HttpServletResponse response) throws IOException {
        String defaultPath = "/images/user-picture.jpg";
        if (!(StringUtils.isEmpty(p) || p.equals(defaultPath))) {
            UploadProjectProperties properties = SpringContextUtil.getBean(UploadProjectProperties.class);
            String fuPath = properties.getFilePath();
            String spPath = properties.getStaticPath();
            File file = new File(fuPath + p.replace(spPath, ""));
            if (file.exists()) {
                FileCopyUtils.copy(new FileInputStream(file), response.getOutputStream());
                return;
            }
        }

        cn.hutool.core.io.resource.Resource resource = new ClassPathResource("static" + defaultPath);
        FileCopyUtils.copy(resource.getStream(), response.getOutputStream());
    }

    /**
     * 导出用户数据
     */
    @GetMapping("/export")
    @RequiresPermissions("system:user:export")
    @ResponseBody
    @ActionLog(name = "导出用户列表信息", title = "用户管理", key = LogTypeConstant.DOWNLOAD)
    public void exportExcel(HttpServletResponse response) {
        List<SysUser> userList = userService.list();
        try {
            SXSSFWorkbook excel = TemplateExportUtil.exportUser(userList);
            DispatchRecordExcelUtil.responseBody(excel, response, "用户信息");
        } catch (Exception e) {
        }
    }

    /**
     * 设置一条或者多条数据的状态
     */
    @RequestMapping("/status/{param}")
    @RequiresPermissions("system:user:status")
    @ResponseBody
    @ActionLog(name = "设置用户状态", title = "用户管理", key = LogTypeConstant.MODIFY)
    public ResultVo updateStatus(
            @PathVariable("param") String param,
            @RequestParam(value = "ids", required = false) List<String> ids) {

        // 不能修改超级管理员状态
        if (ids.contains(AdminConst.ADMIN_ID)) {
            throw new ResultException(ResultEnum.NO_ADMIN_STATUS);
        }

        // 更新状态
        if (userService.updateStatus(param, ids)) {
            return ResultVoUtil.success("更新成功");
        } else {
            return ResultVoUtil.error("更新失败，请重新操作");
        }
    }

}
