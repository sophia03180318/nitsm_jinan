package com.jcca.common.shiro.realm;

import cn.hutool.core.date.DateUtil;
import com.jcca.admin.system.entity.SysMenu;
import com.jcca.admin.system.entity.SysRole;
import com.jcca.admin.system.entity.SysUser;
import com.jcca.admin.system.service.SysMenuService;
import com.jcca.admin.system.service.SysUserService;
import com.jcca.common.bean.constant.AdminConst;
import com.jcca.common.enums.StatusEnum;
import com.jcca.common.shiro.util.ShiroUtil;
import com.jcca.common.utils.SpringContextUtil;
import com.jcca.web2.entity.SysLoginConf;
import com.jcca.web2.service.SysLoginConfService;
import lombok.SneakyThrows;
import org.apache.shiro.authc.*;
import org.apache.shiro.authc.credential.SimpleCredentialsMatcher;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.codec.CodecSupport;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.util.ByteSource;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author hanwone
 * @date 2018/8/14
 */
public class AuthRealm extends AuthorizingRealm {

    /**
     * 授权逻辑
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principal) {
        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
        // 获取用户Principal对象
        SysUser user = (SysUser) principal.getPrimaryPrincipal();

        // 管理员拥有所有权限
        if (user.getId().equals(AdminConst.ADMIN_ID)) {
            info.addRole(AdminConst.ADMIN_ROLE_NAME);
            info.addStringPermission("*:*:*");
            return info;
        }

        // 赋予角色和资源授权
        List<SysRole> roles = ShiroUtil.getSubjectRoles();
        SysMenuService sysMenuService = SpringContextUtil.getBean(SysMenuService.class);
        roles.forEach(role -> {
            info.addRole(role.getName());
            List<SysMenu> menuList = sysMenuService.findMenuByRoleId(role.getId());
            menuList.forEach(menu -> {
                String perms = menu.getPerms();
                if (menu.getStatus().equals(StatusEnum.OK.getCode())
                        && !StringUtils.isEmpty(perms) && !perms.contains("*")) {
                    info.addStringPermission(perms);
                }
            });
        });

        return info;
    }

    /**
     * 认证逻辑
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
        UsernamePasswordToken token = (UsernamePasswordToken) authenticationToken;
        // 获取数据库中的用户名密码
        SysUserService userService = SpringContextUtil.getBean(SysUserService.class);
        SysUser user = userService.findByUsername(token.getUsername());

        // 判断用户名是否存在
        if (user == null) {
            throw new UnknownAccountException();
        }

        // 对盐进行加密处理
        ByteSource salt = ByteSource.Util.bytes(user.getPwdSalt());

        /* 传入密码自动判断是否正确
         * 参数1：传入对象给Principal
         * 参数2：正确的用户密码
         * 参数3：加盐处理
         * 参数4：固定写法
         */
        return new SimpleAuthenticationInfo(user, user.getPassword(), salt, getName());
    }

    /**
     * 自定义密码验证匹配器
     */
    @PostConstruct
    public void initCredentialsMatcher() {
        setCredentialsMatcher(new SimpleCredentialsMatcher() {
            @SneakyThrows
            @Override
            public boolean doCredentialsMatch(AuthenticationToken authenticationToken, AuthenticationInfo authenticationInfo) {
                UsernamePasswordToken token = (UsernamePasswordToken) authenticationToken;
                SimpleAuthenticationInfo info = (SimpleAuthenticationInfo) authenticationInfo;
                // 获取明文密码及密码盐
                String password = String.valueOf(token.getPassword());
                String userName = String.valueOf(token.getUsername());

                String salt = CodecSupport.toString(info.getCredentialsSalt().getBytes());

                boolean equals = equals(ShiroUtil.encrypt(password, salt), info.getCredentials());
                updateLoginMsg(userName, equals);
                return equals;
            }
        });
    }

    /**
     * 更新登录时间和次数
     *
     * @param username
     * @param result
     */
    void updateLoginMsg(String username, boolean result) {
        if (AdminConst.ADMIN_NAME.equals(username)) {
            return;
        }
        SysLoginConfService loginConfService = SpringContextUtil.getBean(SysLoginConfService.class);
        SysUserService userService = SpringContextUtil.getBean(SysUserService.class);
        SysUser user = userService.findByUsername(username);

        SysLoginConf loginConf = loginConfService.getById(user.getId());
        if (Objects.isNull(loginConf)) {
            return;
        }
        Integer trySize = loginConf.getTrySize();
        if (Objects.isNull(trySize)) {
            trySize = 0;
        }

        if (result) {
            loginConf.setTrySize(trySize + 1);
            loginConf.setTrySize(0);
            loginConf.setStatus(StatusEnum.OK.getCode());
            // 解除锁定时修改用户状态为 1:正常
            if (StatusEnum.FREEZED.getCode().byteValue() == user.getStatus()) {
                userService.updateStatus(StatusEnum.OK.name(), Collections.singletonList(user.getId()));
            }
        } else {
            loginConf.setTrySize(trySize + 1);
        }
        loginConf.setLastTime(DateUtil.now());
        loginConfService.updateById(loginConf);
    }

}
