package com.jcca.common.shiro;

import cn.hutool.json.JSONUtil;
import com.jcca.admin.system.entity.SysUser;
import com.jcca.common.bean.constant.AdminConst;
import com.jcca.common.enums.ResultEnum;
import com.jcca.web.auth.constant.TokenConst;
import com.jcca.web.auth.util.TokenUtil;
import org.apache.shiro.web.filter.AccessControlFilter;
import org.apache.shiro.web.util.WebUtils;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.Base64;

/**
 * 处理session超时问题拦截器
 *
 * @author hanwone
 * @date 2018/8/14
 */
public class UserAuthFilter extends AccessControlFilter {

    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {
        HttpServletRequest httpServletRequest = WebUtils.toHttp(request);
        String requestURI = httpServletRequest.getRequestURI();
        if (isLoginRequest(request, response) || AdminConst.OUT_LOGIN_URI.equals(requestURI)
                || AdminConst.OUT_QUIT_URI.equals(requestURI)) {
            return true;
        } else {
            Object principal = getSubject(request, response).getPrincipal();
            if (principal != null) {
                SysUser sysUser = (SysUser) principal;
                if (AdminConst.ADMIN_NAME.equals(sysUser.getUsername())) {
                    return true;
                }
                if (requestURI.startsWith(AdminConst.OUT_USER_URI)) {
                    String token = TokenUtil.getRequestToken(httpServletRequest);
                    if (!StringUtils.isEmpty(token)) {
                        return TokenUtil.verifyToken(new String(Base64.getDecoder().decode(token.getBytes())), TokenConst.TOKEN_SECRECT);
                    }
                }
            }
            return principal != null;
        }
    }

    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
        HttpServletRequest httpRequest = WebUtils.toHttp(request);
        HttpServletResponse httpResponse = WebUtils.toHttp(response);
        String requestURI = httpRequest.getRequestURI();
        if (requestURI.startsWith(AdminConst.OUT_USER_URI)) {
            httpResponse.setStatus(HttpStatus.OK.value());
            httpResponse.setCharacterEncoding("utf8");
            PrintWriter writer = httpResponse.getWriter();
            writer.println(JSONUtil.toJsonStr(ResultEnum.NEED_LOGIN));
        } else {
            if (httpRequest.getHeader("X-Requested-With") != null
                    && "XMLHttpRequest".equalsIgnoreCase(httpRequest.getHeader("X-Requested-With"))) {
                httpResponse.sendError(HttpStatus.UNAUTHORIZED.value());
            } else {
                redirectToLogin(request, response);
            }
        }
        return false;
    }
}
