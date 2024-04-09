package com.jcca.web2.controller;

import com.jcca.admin.system.entity.SysUser;
import com.jcca.admin.system.service.SysUserService;
import com.jcca.common.bean.ResultVo;
import com.jcca.common.bean.constant.AdminConst;
import com.jcca.common.enums.ResultEnum;
import com.jcca.common.enums.StatusEnum;
import com.jcca.common.exception.ResultException;
import com.jcca.common.log.annotation.ActionLog;
import com.jcca.common.log.constant.LogTypeConstant;
import com.jcca.common.utils.ResultVoUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;

/**
 * @author HanHW
 * @description 用户管理V2
 * @className UserControllerV2
 * @date 2023/10/19 15:17
 * @since 2.1.0.0
 */
@RestController
@RequestMapping("/api/v2/user")
@Api(tags = "用户管理V2")
public class UserControllerV2 {

    @Resource
    private SysUserService userService;

    @PostMapping("/list")
    @ApiOperation("获取用户列表")
    @RequiresPermissions("api:v2:user:list")
    public ResultVo<Object> getList(@RequestBody SysUser user) {

        List<SysUser> userList = userService.getUserListV2(user);

        return ResultVoUtil.success(userList);
    }

    @PostMapping("/save")
    @ApiOperation("保存用户信息")
    @RequiresPermissions("api:v2:user:save")
    @ActionLog(name = "保存用户信息", title = "用户管理", key = LogTypeConstant.ADD)
    public ResultVo<Object> saveUser(@RequestBody @Validated SysUser user) {

        userService.saveUserV2(user);

        return ResultVoUtil.success();
    }

    @GetMapping("/detail/{id}")
    @ApiOperation("用户详情")
    @RequiresPermissions("api:v2:user:detail")
    public ResultVo<Object> detail(@PathVariable("id") String id) {

        SysUser user = userService.findDetailV2(id);

        return ResultVoUtil.success(user);
    }

    @PostMapping("/pwd")
    @ApiOperation("修改密码")
    @RequiresPermissions("api:v2:user:pwd")
    @ActionLog(name = "修改密码", title = "用户管理", key = LogTypeConstant.MODIFY)
    public ResultVo<Object> editPassword(@RequestBody SysUser user) {

        String id = user.getId();
        String password = user.getPassword();
        String confirm = user.getConfirm();
        userService.editPasswordV2(id, password, confirm);

        return ResultVoUtil.success();
    }

    @PostMapping("/delete/{id}")
    @ApiOperation("删除用户")
    @RequiresPermissions("api:v2:user:delete")
    @ActionLog(name = "删除用户", title = "用户管理", key = LogTypeConstant.REMOVEE)
    public ResultVo<Object> deleteUser(@PathVariable("id") String id) {

        // 不能修改超级管理员状态
        if (AdminConst.ADMIN_ID.equals(id)) {
            throw new ResultException(ResultEnum.NO_ADMIN_STATUS);
        }

        // 更新状态
        if (userService.updateStatus(StatusEnum.DELETE.name(), Collections.singletonList(id))) {
            return ResultVoUtil.success(ResultEnum.SUCCESS.getMessage());
        } else {
            return ResultVoUtil.error(ResultEnum.UPDATE_FAIL.getMessage());
        }
    }
}
