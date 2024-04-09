package com.jcca.admin.system.controller;

import com.jcca.admin.system.config.SystemProperties;
import com.jcca.admin.system.entity.SysUser;
import com.jcca.admin.system.service.SysUserService;
import com.jcca.common.bean.ResultVo;
import com.jcca.common.bean.constant.AdminConst;
import com.jcca.common.enums.ResultEnum;
import com.jcca.common.exception.ResultException;
import com.jcca.common.input.LogInputUtils;
import com.jcca.common.input.ServerTypeEnum;
import com.jcca.common.log.annotation.ActionLog;
import com.jcca.common.log.constant.LogTypeConstant;
import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.utils.*;
import com.jcca.web.auth.service.LoginSecurityService;
import com.jcca.web.auth.service.impl.LoginSecurityServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.LockedAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.Objects;

/**
 * @author 小白龙
 * @date 2018/8/14
 */
@Controller
@Slf4j
public class LoginController implements ErrorController {

    @Resource
    private LoginSecurityService loginSecurityServ;
    @Resource
    private SysUserService userService;

    /**
     * 跳转到登录页面
     */
    @GetMapping("/login")
    public String toLogin(Model model) {
        SystemProperties properties = SpringContextUtil.getBean(SystemProperties.class);
        model.addAttribute("isCaptcha", properties.isCaptchaOpen());
        return "/login";
    }

    /**
     * 实现登录
     */
    @PostMapping("/login")
    @ResponseBody
    @ActionLog(name = "后台用户登录", title = "用户登录", key = LogTypeConstant.LOGIN_OUT)
    public ResultVo login(String username, String password, String captcha, String rememberMe, HttpServletRequest request) {
        // 判断账号密码是否为空
        if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password)) {
            throw new ResultException(ResultEnum.USERNAME_PWD_NULL);
        }
        // 判断验证码是否正确
        SystemProperties properties = SpringContextUtil.getBean(SystemProperties.class);
        if (properties.isCaptchaOpen()) {
            Session session = SecurityUtils.getSubject().getSession();
            String sessionCaptcha = (String) session.getAttribute("captcha");
            if (StringUtils.isEmpty(captcha) || StringUtils.isEmpty(sessionCaptcha)
                    || !captcha.toUpperCase().equals(sessionCaptcha.toUpperCase())) {
                throw new ResultException(ResultEnum.USER_CAPTCHA_ERROR);
            }
            session.removeAttribute("captcha");
        }

        // 根据用户名获取系统用户数据
        SysUser user = userService.findByUsername(username);
        if (Objects.isNull(user)) {
            return ResultVoUtil.error(ResultEnum.USER_EXIST_ERROR.getCode(), ResultEnum.USER_EXIST_ERROR.getMessage());
        }

        //增加登录用户安全审计
        String remoteAddr = LoginSecurityServiceImpl.getIpAddr(request);
        try {
            loginSecurityServ.securityVerify(user.getId(), remoteAddr);
        } catch (ResultException e) {
            return ResultVoUtil.error(ResultEnum.LOGIN_SECURITY_ERROR.getCode(), e.getMessage());
        } catch (Exception e) {
            return ResultVoUtil.error(ResultEnum.LOGIN_SECURITY_ERROR.getCode(), ResultEnum.LOGIN_SECURITY_ERROR.getMessage());
        }

        // 1.获取Subject主体对象
        Subject subject = SecurityUtils.getSubject();

        // 2.封装用户数据
        UsernamePasswordToken token = new UsernamePasswordToken(username, password);

        // 3.执行登录，进入自定义Realm类中
        try {
            // 判断是否自动登录
            if (rememberMe != null) {
                token.setRememberMe(true);
            } else {
                token.setRememberMe(false);
            }

            // 同一账号多处登录判断
            DefaultWebSecurityManager securityManager = (DefaultWebSecurityManager) SecurityUtils.getSecurityManager();
            DefaultWebSessionManager sessionManager = (DefaultWebSessionManager) securityManager.getSessionManager();
            Collection<Session> activeSessions = sessionManager.getSessionDAO().getActiveSessions();
            for (Session session : activeSessions) {
                if (username.equals(session.getAttribute(AdminConst.LOGIN_USER_M))) {
                    if (LogInputUtils.inputInfo(ServerTypeEnum.SYSTEM_LOGIN)) {
                        log.info(LogInputUtils.formattingInfoLog(ServerTypeEnum.SYSTEM_LOGIN, "", "后台用户" + username + "已登录强制退出前一次登录"));
                    }
                    subject.logout();
                    sessionManager.getSessionDAO().delete(session);
                }
            }

            subject.login(token);
            AppLogUtils.buildLogInfo(LogFunctionEnum.LOGIN_WEB, username, "后台登录成功，用户IP：" + remoteAddr);
            subject.getSession().setAttribute(AdminConst.LOGIN_USER_M, username);

            return ResultVoUtil.success("登录成功", new URL("/"));
        } catch (LockedAccountException e) {
            return ResultVoUtil.error(ResultEnum.ACCOUNT_FREEZED.getCode(), ResultEnum.ACCOUNT_FREEZED.getMessage());
        } catch (AuthenticationException e) {
            return ResultVoUtil.error(ResultEnum.USERNAME_PWD_ERROR.getCode(), ResultEnum.USERNAME_PWD_ERROR.getMessage());
        }
    }

    /**
     * 验证码图片
     */
    @GetMapping("/captcha")
    public void captcha(HttpServletRequest request, HttpServletResponse response) throws IOException {
        //设置响应头信息，通知浏览器不要缓存
        response.setHeader("Expires", "-1");
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Pragma", "-1");
        response.setContentType("image/jpeg");

        // 获取验证码
        String code = CaptchaUtil.getRandomCode();
        // 将验证码输入到session中，用来验证
        request.getSession().setAttribute("captcha", code);
        // 输出到web页面
        ImageIO.write(CaptchaUtil.genCaptcha(code), "jpg", response.getOutputStream());
    }

    /**
     * 退出登录
     */
    @GetMapping("/logout")
    @ActionLog(name = "后台用户退出", title = "用户登录", key = LogTypeConstant.LOGIN_OUT)
    public String logout() {
        SecurityUtils.getSubject().logout();
        return "redirect:/login";
    }

    /**
     * 权限不足页面
     */
    @GetMapping("/noAuth")
    public String noAuth() {
        return "/system/main/noAuth";
    }

    /**
     * 自定义错误页面
     */
    @Override
    public String getErrorPath() {
        return "/error";
    }

    /**
     * 处理错误页面
     */
    @RequestMapping("/error")
    public String handleError(Model model, HttpServletRequest request) {
        Integer statusCode = (Integer) request.getAttribute("javax.servlet.error.status_code");
        String errorMsg = "请联系管理员！";
        if (statusCode == 404) {
            errorMsg = "页面找不到了，联系管理员去找页面吧~";
        }

        model.addAttribute("statusCode", statusCode);
        model.addAttribute("msg", errorMsg);
        return "/system/main/error";
    }
}
