package com.jcca.admin.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.jcca.admin.system.entity.*;
import com.jcca.admin.system.service.SysMenuService;
import com.jcca.admin.system.service.SysRoleMenuService;
import com.jcca.admin.system.service.SysUserService;
import com.jcca.admin.system.validator.UserValid;
import com.jcca.common.bean.ResultVo;
import com.jcca.common.bean.constant.AdminConst;
import com.jcca.common.enums.MenuTypeEnum;
import com.jcca.common.enums.ResultEnum;
import com.jcca.common.enums.StatusEnum;
import com.jcca.common.exception.ResultException;
import com.jcca.common.log.annotation.ActionLog;
import com.jcca.common.log.constant.LogTypeConstant;
import com.jcca.common.shiro.util.ShiroUtil;
import com.jcca.common.utils.*;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author hanwone
 * @date 2018/8/14
 */
@Controller
public class MainController {

    @Resource
    private SysUserService userService;

    @Resource
    private SysMenuService menuService;

    @Resource
    private SysRoleMenuService roleMenuService;

    /**
     * 后台主体内容
     */
    @GetMapping("/")
    @RequiresPermissions("index")
    public String main(Model model) {
        // 处理导入菜单数据时因排序序号相同页面不显示导入菜单的问题 20221101 godwone
        QueryWrapper<SysMenu> query = Wrappers.query();
        query.eq("status", StatusEnum.OK.getCode());
        this.resorted(menuService.list(query));

        // 获取当前登录的用户
        SysUser user = ShiroUtil.getSubject();

        // 菜单键值对(ID->菜单)
        Map<String, SysMenu> keyMenu = new HashMap<>(16);

        // 管理员实时更新菜单
        Collection<SysMenu> menus;
        if (user.getId().equals(AdminConst.ADMIN_ID)) {
            menus = menuService.getListBySortOk();
        } else {
            // 其他用户需从相应的角色中获取菜单资源
            List<SysRole> roles = ShiroUtil.getSubjectRoles();
            List<String> menuIdList = new ArrayList<>();
            roles.forEach(role -> {
                QueryWrapper<SysRoleMenu> roleMenuQueryWrapper = new QueryWrapper<>();
                roleMenuQueryWrapper.eq("role_id", role.getId());
                List<SysRoleMenu> roleMenuList = roleMenuService.list(roleMenuQueryWrapper);
                roleMenuList.forEach(roleMenu -> {
                    if (!menuIdList.contains(roleMenu.getMenuId())) {
                        menuIdList.add(roleMenu.getMenuId());
                    }
                });
            });
            menus = menuService.listByIds(menuIdList);
        }

        menus.forEach(menu -> {
            if (menu.getStatus().equals(StatusEnum.OK.getCode())) {
                keyMenu.put(menu.getId(), menu);
            }
        });

        // 封装菜单树形数据
        Map<String, SysMenu> treeMenu = new HashMap<>(16);
        keyMenu.forEach((id, menu) -> {
            if (!menu.getType().equals(MenuTypeEnum.BUTTON.getCode())) {
                if (keyMenu.get(menu.getPid()) != null) {
                    Map<Long, SysMenu> children = keyMenu.get(menu.getPid()).getChildren();
                    Long sort = Long.valueOf(menu.getSort());
                    children.put(sort, menu);
                } else {
                    if (menu.getType().equals(MenuTypeEnum.DIRECTORY.getCode())) {
                        treeMenu.put(menu.getSort().toString(), menu);
                    }
                }
            }
        });

        model.addAttribute("user", user);
        model.addAttribute("treeMenu", treeMenu);
        return "/main";
    }


    private void resorted(Collection<SysMenu> menus) {
        List<SysMenu> list = new ArrayList<>();
        // 按pid分类
        Map<String, List<SysMenu>> collect = menus.stream().collect(Collectors.groupingBy(SysMenu::getPid));
        Set<String> keys = collect.keySet();
        for (String key : keys) {
            // 按原sort正序排序
            List<SysMenu> ms = collect.get(key).stream().sorted(Comparator.comparing(SysMenu::getSort)).collect(Collectors.toList());
            byte flag = 1;
            for (SysMenu menu : ms) {
                if (flag != menu.getSort()) {
                    SysMenu m = new SysMenu();
                    BeanUtils.copyProperties(menu, m);
                    m.setSort(flag);
                    list.add(m);
                }
                flag += 1;
            }
        }
        if (!CollectionUtils.isEmpty(list)) {
            menuService.saveOrUpdateBatch(list);
        }
    }

    /**
     * 主页
     */
    @GetMapping("/index")
    @RequiresPermissions("index")
    public String index(Model model) {
        String search = HttpServletUtil.getRequest().getQueryString();
        model.addAttribute("search", search);
        return "/system/main/index";
    }


    /**
     * 跳转到个人信息页面
     */
    @GetMapping("/userInfo")
    @RequiresPermissions("index")
    public String toUserInfo(Model model) {
        model.addAttribute("user", ShiroUtil.getSubject());
        return "/system/main/userInfo";
    }

    /**
     * 修改用户头像
     */
    @PostMapping("/userPicture")
    @RequiresPermissions("index")
    @ResponseBody
    public ResultVo userPicture(@RequestParam("picture") MultipartFile picture) {
        UploadController uploadController = SpringContextUtil.getBean(UploadController.class);
        ResultVo imageResult = uploadController.uploadPicture(picture);
        if (imageResult.getCode().equals(ResultEnum.SUCCESS.getCode())) {
            SysUser subject = ShiroUtil.getSubject();
            subject.setPicture(((SysFile) imageResult.getData()).getFilePath());
            userService.saveOrUpdate(subject);
            return ResultVoUtil.SAVE_SUCCESS;
        } else {
            return imageResult;
        }
    }

    /**
     * 保存修改个人信息
     */
    @PostMapping("/userInfo")
    @RequiresPermissions("index")
    @ResponseBody
    public ResultVo userInfo(@Validated UserValid valid, SysUser user) {

        // 复制保留无需修改的数据
        SysUser subUser = ShiroUtil.getSubject();
        String[] ignores = {"id", "username", "password", "pwdSalt", "picture", "org", "roles", "creator"};
        EntityBeanUtil.copyPropertiesIgnores(user, subUser, ignores);

        // 保存数据
        userService.saveOrUpdate(subUser);
        return ResultVoUtil.success("保存成功", new URL("/userInfo"));
    }

    /**
     * 跳转到修改密码页面
     */
    @GetMapping("/editPwd")
    @RequiresPermissions("index")
    public String toEditPwd() {
        return "/system/main/editPwd";
    }

    /**
     * 保存修改密码
     */
    @PostMapping("/editPwd")
    @RequiresPermissions("index")
    @ResponseBody
    @ActionLog(name = "修改用户密码", title = "后台主页", key = LogTypeConstant.LOGIN_OUT)
    public ResultVo editPwd(String original, String password, String confirm) {
        // 判断原来密码是否有误
        SysUser subUser = ShiroUtil.getSubject();
        String oldPwd = null;
        try {
            oldPwd = ShiroUtil.encrypt(original, subUser.getPwdSalt());
        } catch (UnsupportedEncodingException | NoSuchAlgorithmException e) {
            throw new ResultException(ResultEnum.PWD_ENCRYT_ERROR);
        }
        if (original.isEmpty() || "".equals(original.trim()) || !oldPwd.equals(subUser.getPassword())) {
            throw new ResultException(ResultEnum.USER_OLD_PWD_ERROR);
        }

        // 判断密码是否为空
        if (password.isEmpty() || "".equals(password.trim())) {
            throw new ResultException(ResultEnum.USERNAME_PWD_NULL);
        }

        // 判断两次密码是否一致
        if (!password.equals(confirm)) {
            throw new ResultException(ResultEnum.USER_INEQUALITY);
        }

        // 修改密码，对密码进行加密
        String salt = ShiroUtil.getRandomSalt();
        String encrypt = null;
        try {
            encrypt = ShiroUtil.encrypt(password, salt);
        } catch (UnsupportedEncodingException | NoSuchAlgorithmException e) {
            throw new ResultException(ResultEnum.PWD_ENCRYT_ERROR);
        }
        subUser.setPassword(encrypt);
        subUser.setPwdSalt(salt);

        // 保存数据
        userService.saveOrUpdate(subUser);
        return ResultVoUtil.success("修改成功");
    }
}
