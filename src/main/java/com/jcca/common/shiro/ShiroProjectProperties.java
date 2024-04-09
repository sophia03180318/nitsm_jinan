package com.jcca.common.shiro;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 项目-shiro会话配置项
 *
 * @date 2018/11/6
 */
@Data
@Component
public class ShiroProjectProperties {

    /**
     * cookie记住登录信息时间，天
     */
    @Value("${project.shiro.remember-me-timeout}")
    private Integer rememberMeTimeout;

    /**
     * Session会话超时时间，秒
     */
    @Value("${project.shiro.global-session-timeout}")
    private Integer globalSessionTimeout;

    /**
     * Session会话检测间隔时间，秒
     */
    @Value("${project.shiro.seeeion-validation-interval}")
    private Integer sessionValidationInterval;

    /**
     * 忽略的路径规则，多个规则使用","逗号隔开
     */
    @Value("${project.shiro.excludes}")
    private String excludes = "";
}
