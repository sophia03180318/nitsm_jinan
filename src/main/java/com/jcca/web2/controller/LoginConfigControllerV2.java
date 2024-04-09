package com.jcca.web2.controller;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.jcca.admin.system.service.SysUserService;
import com.jcca.common.bean.ResultVo;
import com.jcca.common.bean.constant.AdminConst;
import com.jcca.common.enums.ResultEnum;
import com.jcca.common.enums.StatusEnum;
import com.jcca.common.exception.ResultException;
import com.jcca.common.log.annotation.ActionLog;
import com.jcca.common.log.constant.LogTypeConstant;
import com.jcca.common.shiro.util.ShiroUtil;
import com.jcca.common.utils.ResultVoUtil;
import com.jcca.web.auth.controller.bean.LoginConfBean;
import com.jcca.web2.entity.SysLoginConf;
import com.jcca.web2.service.SysLoginConfService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author HanHW
 * @description 用户登录限制
 * @className LoginConfigController
 * @date 2023/10/19 11:42
 * @since 2.1.0.0
 */
@RestController
@RequestMapping("/api/v2/login")
@Api(tags = "用户登录配置V2")
public class LoginConfigControllerV2 {


    @Resource
    private SysUserService userService;
    @Resource
    private SysLoginConfService loginConfService;

    @GetMapping("/getLoginConfig/{userId}")
    @ApiOperation("查看登录配置")
    @RequiresPermissions("api:v2:login:getLoginConfig")
    public ResultVo<Object> getLoginConfig(@PathVariable("userId") String userId) {

        if (StringUtils.isEmpty(userId)) {
            return ResultVoUtil.error(ResultEnum.PARAM_ERROR.getCode(), "用户ID不能为空");
        }

        SysLoginConf loginConf = loginConfService.getById(userId);

        if (Objects.isNull(loginConf)) {
            return ResultVoUtil.success();
        }

        if (!StringUtils.isEmpty(loginConf.getIpList())) {
            JSONArray jsonArray = JSONUtil.parseArray(loginConf.getIpList());
            List<String> ips = JSONUtil.toList(jsonArray, String.class);
            loginConf.setIps(ips);
        }
        return ResultVoUtil.success(loginConf);
    }

    @PostMapping("/setLoginConfig")
    @ApiOperation("设置登录配置")
    @RequiresPermissions("api:v2:login:setLoginConfig")
    @ActionLog(name = "设置登录配置", title = "登录配置", key = LogTypeConstant.MODIFY)
    public ResultVo<Object> setLoginConfig(@RequestBody @Validated LoginConfBean loginConfBean) {

        loginConfService.setLoginConf(loginConfBean);

        return ResultVoUtil.success();
    }

    @PostMapping("/unlock")
    @ApiOperation("解除登录锁定")
    @RequiresPermissions("api:v2:login:unlock")
    @ActionLog(name = "解除登录锁定", title = "登录配置", key = LogTypeConstant.MODIFY)
    public ResultVo<Object> unlock(@RequestBody @Validated LoginConfBean loginConfBean) {

        if (!ShiroUtil.getSubject().getId().equals(AdminConst.ADMIN_ID)) {
            throw new ResultException(ResultEnum.ONLY_ADMINROLE_AUTH);
        }

        String userId = loginConfBean.getUserId();
        SysLoginConf loginConf = loginConfService.getById(userId);
        if (Objects.isNull(loginConf)) {
            return ResultVoUtil.warning("未配置相应登录限制");
        }

        loginConf.setTrySize(0);
        loginConf.setStatus(StatusEnum.OK.getCode());
        loginConfService.updateById(loginConf);

        // 解除锁定时修改用户状态为 1:正常
        userService.updateStatus(StatusEnum.OK.name(), Collections.singletonList(userId));

        return ResultVoUtil.success();
    }

}
