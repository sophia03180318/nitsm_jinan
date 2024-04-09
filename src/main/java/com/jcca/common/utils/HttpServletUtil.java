package com.jcca.common.utils;

import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 获取HttpServlet子对象
 *
 * @date 2018/10/15
 */
public class HttpServletUtil {

    /**
     * 获取ServletRequestAttributes对象
     */
    public static ServletRequestAttributes getServletRequest() {
        return (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
    }

    /**
     * 获取HttpServletRequest对象
     */
    public static HttpServletRequest getRequest() {
        return getServletRequest().getRequest();
    }

    /**
     * 获取HttpServletResponse对象
     */
    public static HttpServletResponse getResponse() {
        return getServletRequest().getResponse();
    }

    /**
     * 获取请求参数
     */
    public static String getParameter(String param) {
        return getRequest().getParameter(param);
    }

    /**
     * 获取请求参数，带默认值
     */
    public static String getParameter(String param, String defaultValue) {
        String parameter = getRequest().getParameter(param);
        return StringUtils.isEmpty(parameter) ? defaultValue : parameter;
    }
}
