package com.jcca.common.shiro.config;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.jcca.common.shiro.ShiroProjectProperties;
import com.jcca.common.shiro.UserAuthFilter;
import com.jcca.common.shiro.realm.AuthRealm;
import net.sf.ehcache.CacheManager;
import org.apache.shiro.cache.ehcache.EhCacheManager;
import org.apache.shiro.codec.Base64;
import org.apache.shiro.session.mgt.eis.MemorySessionDAO;
import org.apache.shiro.session.mgt.eis.SessionDAO;
import org.apache.shiro.spring.LifecycleBeanPostProcessor;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.CookieRememberMeManager;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.servlet.SimpleCookie;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.Filter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @ClassName ShiroConfig
 * @Description TODO
 * @Date 2020/4/6 12:32
 * @Author hanwone
 */
@Configuration
public class ShiroConfig {

    @Bean
    public ShiroFilterFactoryBean getShiroFilterFactoryBean(DefaultWebSecurityManager securityManager, ShiroProjectProperties properties) {
        ShiroFilterFactoryBean shiroFilterFactoryBean = new ShiroFilterFactoryBean();
        shiroFilterFactoryBean.setSecurityManager(securityManager);

        /**
         * 添加自定义拦截器，重写user认证方式，处理session超时问题
         */
        Map<String, Filter> myFilters = new HashMap<>(16);
        myFilters.put("userAuth", new UserAuthFilter());
        shiroFilterFactoryBean.setFilters(myFilters);

        /**
         *  过滤规则（注意优先级）
         *  —anon 无需认证(登录)可访问
         * 	—authc 必须认证才可访问
         * 	—perms[标识] 拥有资源权限才可访问
         * 	—role 拥有角色权限才可访问
         * 	—user 认证和自动登录可访问
         */
        Map<String, String> filterMap = new LinkedHashMap<>();
        filterMap.put("/login", "anon");
        filterMap.put("/api/auth", "anon");
        filterMap.put("/logout", "anon");
        filterMap.put("/captcha", "anon");
        filterMap.put("/ws/**", "anon");
        filterMap.put("/noAuth", "anon");
        filterMap.put("/css/**", "anon");
        filterMap.put("/js/**", "anon");
        filterMap.put("/images/**", "anon");
        filterMap.put("/lib/**", "anon");
        filterMap.put("/favicon.ico", "anon");
        // 通过yml配置文件方式配置的[anon]忽略规则
        String[] excludes = properties.getExcludes().split(",");
        for (String exclude : excludes) {
            if (!StringUtils.isEmpty(exclude.trim())) {
                filterMap.put(exclude, "anon");
            }
        }
        // 拦截根目录下所有路径，需要放行的路径必须在之前添加
        filterMap.put("/**", "userAuth");

        // 设置过滤规则
        shiroFilterFactoryBean.setFilterChainDefinitionMap(filterMap);
        // 设置登录页面
        shiroFilterFactoryBean.setLoginUrl("/login");
        // 未授权错误页面
        shiroFilterFactoryBean.setUnauthorizedUrl("/noAuth");

        return shiroFilterFactoryBean;
    }

    @Bean
    public DefaultWebSecurityManager getDefaultWebSecurityManager(AuthRealm authRealm,
                                                                  EhCacheManager cacheManager,
                                                                  DefaultWebSessionManager sessionManager,
                                                                  CookieRememberMeManager rememberMeManager) {
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
        securityManager.setRealm(authRealm);
        securityManager.setCacheManager(cacheManager);
        securityManager.setSessionManager(sessionManager);
        securityManager.setRememberMeManager(rememberMeManager);
        return securityManager;
    }

    /**
     * 自定义的Realm
     */
    @Bean
    public AuthRealm getRealm(EhCacheManager ehCacheManager) {
        AuthRealm authRealm = new AuthRealm();
        authRealm.setCacheManager(ehCacheManager);
        return authRealm;
    }

    /**
     * 缓存管理器-使用Ehcache实现缓存
     */
    @Bean
    public EhCacheManager ehCacheManager(CacheManager cacheManager) {
        EhCacheManager ehCacheManager = new EhCacheManager();
        ehCacheManager.setCacheManager(cacheManager);
        return ehCacheManager;
    }

    /**
     * session管理器
     */
    @Bean
    public DefaultWebSessionManager defaultWebSessionManager(EhCacheManager cacheManager, ShiroProjectProperties properties) {
        DefaultWebSessionManager sessionManager = new DefaultWebSessionManager();
        sessionManager.setCacheManager(cacheManager);
        sessionManager.setGlobalSessionTimeout(properties.getGlobalSessionTimeout() * 1000);
        sessionManager.setSessionValidationInterval(properties.getSessionValidationInterval() * 1000);
        sessionManager.setDeleteInvalidSessions(true);
        sessionManager.setSessionDAO(sessionDAO());
        sessionManager.validateSessions();
        // 去掉登录页面地址栏jsessionid
        sessionManager.setSessionIdUrlRewritingEnabled(false);

        sessionManager.setSessionIdCookie(sessionCookie());

        return sessionManager;
    }

    @Bean
    public SimpleCookie sessionCookie() {
        SimpleCookie cookie = new SimpleCookie("webSessionId");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(-1);
        return cookie;
    }

    @Bean
    public SessionDAO sessionDAO() {
        return new MemorySessionDAO();
    }

    /**
     * rememberMe管理器
     */
    @Bean
    public CookieRememberMeManager rememberMeManager(SimpleCookie rememberMeCookie) {
        CookieRememberMeManager manager = new CookieRememberMeManager();
        manager.setCipherKey(Base64.decode("2lSGtJUSE6GLUJfNB3CYcCiEKIJK99SaJq989oEaqz"));
        manager.setCookie(rememberMeCookie);
        return manager;
    }

    /**
     * 创建一个简单的Cookie对象
     */
    @Bean
    public SimpleCookie rememberMeCookie(ShiroProjectProperties properties) {
        SimpleCookie simpleCookie = new SimpleCookie("rememberMe");
        simpleCookie.setHttpOnly(true);
        // cookie记住登录信息时间，默认7天
        simpleCookie.setMaxAge(properties.getRememberMeTimeout() * 24 * 60 * 60);
        return simpleCookie;
    }

    @Bean
    public LifecycleBeanPostProcessor lifecycleBeanPostProcessor() {
        return new LifecycleBeanPostProcessor();
    }

    /**
     * 启用shrio授权注解拦截方式，AOP式方法级权限检查
     */
    @Bean
    public AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor(DefaultWebSecurityManager securityManager) {
        AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor =
                new AuthorizationAttributeSourceAdvisor();
        authorizationAttributeSourceAdvisor.setSecurityManager(securityManager);
        return authorizationAttributeSourceAdvisor;
    }

}

