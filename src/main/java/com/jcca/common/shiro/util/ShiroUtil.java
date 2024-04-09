package com.jcca.common.shiro.util;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.jcca.admin.system.entity.SysOrg;
import com.jcca.admin.system.entity.SysRole;
import com.jcca.admin.system.entity.SysUser;
import com.jcca.admin.system.service.SysMenuService;
import com.jcca.admin.system.service.SysOrgService;
import com.jcca.admin.system.service.SysRoleService;
import com.jcca.common.bean.constant.AdminConst;
import com.jcca.common.enums.StatusEnum;
import com.jcca.common.utils.EncryptUtil;
import com.jcca.common.utils.HttpServletUtil;
import com.jcca.common.utils.SpringContextUtil;
import com.jcca.web.asset.entity.AssetAttach;
import com.jcca.web.asset.service.AssetAttachService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.shiro.SecurityUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Shiro工具类
 *
 * @author hanwone
 * @date 2018/8/14
 */
public class ShiroUtil {

    /**
     * 加密算法
     */
    public final static String HASH_ALGORITHM_NAME = EncryptUtil.HASH_ALGORITHM_NAME;

    /**
     * 循环次数
     */
    public final static int HASH_ITERATIONS = EncryptUtil.HASH_ITERATIONS;

    /**
     * 加密处理（64位字符） 备注：采用自定义的密码加密方式，其原理与SimpleHash一致， 为的是在多个模块间可以使用同一套加密方式，方便共用系统用户。
     *
     * @param password 密码
     * @param salt     密码盐
     */
    public static String encrypt(String password, String salt) throws UnsupportedEncodingException, NoSuchAlgorithmException {
        return EncryptUtil.encrypt(password, salt, HASH_ALGORITHM_NAME, HASH_ITERATIONS);
    }

    /**
     * 获取随机盐值
     */
    public static String getRandomSalt() {
        return EncryptUtil.getRandomSalt();
    }

    /**
     * 获取当前用户对象
     */
    public static SysUser getSubject() {
        return (SysUser) SecurityUtils.getSubject().getPrincipal();
    }

    /**
     * 获取当前用户角色列表
     */
    public static List<SysRole> getSubjectRoles() {
        SysUser user = (SysUser) SecurityUtils.getSubject().getPrincipal();
        SysRoleService sysRoleService = SpringContextUtil.getBean(SysRoleService.class);

        return sysRoleService.findRoleByUserId(user.getId());
    }

    /**
     * 获取当前用户角色ID列表
     */
    public static List<String> getSubjectRoleIds() {
        List<SysRole> roles = getSubjectRoles();
        return roles.stream().map(SysRole::getId).collect(Collectors.toList());
    }

    /**
     * 获取当前用户的组织列表
     *
     * @return
     */
    public static List<SysOrg> getSubjectOrgs() {
        SysUser user = (SysUser) SecurityUtils.getSubject().getPrincipal();
        SysOrgService sysOrgService = SpringContextUtil.getBean(SysOrgService.class);
        if (user.getId().equals(AdminConst.ADMIN_ID)) {
            QueryWrapper<SysOrg> wrapper = Wrappers.query();
            wrapper.eq("status", StatusEnum.OK.getCode());
            wrapper.orderByAsc("id");
            return sysOrgService.list(wrapper);
        }
        Map<String, Object> map = new HashMap<>();
        map.put("userId", user.getId());
        return sysOrgService.getOrgsByUserId(map);
    }

    /**
     * 获取当前用户组织ID列表
     */
    public static List<String> getSubjectOrgIds() {
        List<SysOrg> orgs = getSubjectOrgs();
        return orgs.stream().map(SysOrg::getId).collect(Collectors.toList());
    }

    /**
     * 获取当前用户所管理资产ID列表
     */
    public static List<String> getSubjectAssetIds() {
        List<String> orgIds = getSubjectOrgIds();
        if (CollectionUtils.isEmpty(orgIds)) {
            return new ArrayList<>();
        }
        AssetAttachService attachService = SpringContextUtil.getBean(AssetAttachService.class);
        QueryWrapper<AssetAttach> wrapper = Wrappers.query();
        wrapper.in("org_id", orgIds);
        List<AssetAttach> attachList = attachService.list(wrapper);
        return attachList.stream().map(AssetAttach::getAssetId).collect(Collectors.toList());
    }

    /**
     * 获取当前用户的权限列表
     *
     * @return
     */
    public static Set<String> getSubjectPerms() {
        SysUser user = (SysUser) SecurityUtils.getSubject().getPrincipal();
        SysMenuService menuService = SpringContextUtil.getBean(SysMenuService.class);
        if (user.getId().equals(AdminConst.ADMIN_ID)) {
            // Set<String> aset = new HashSet<>();
            // aset.add("*:*:*");
            return menuService.getAllPerms();
        }
        return menuService.getPermsByUserId(user.getId());
    }

    /**
     * 获取用户IP地址
     */
    public static String getIp() {
        HttpServletRequest request = HttpServletUtil.getRequest();
        // 反向代理时获取真实ip
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Forwarded-For");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    /**
     * @description: 获取当前用户的前端目录列表
     * @author: HanHW
     * @date: 2023/10/27 13:30
     * @param: []
     * @return: java.util.Set<java.lang.String>
     **/
    public static Set<String> getSubjectDirsV2() {
        SysUser user = getSubject();
        String userId = user.getId();
        if (userId.equals(AdminConst.ADMIN_ID)) {
            userId = "";
        }
        SysMenuService menuService = SpringContextUtil.getBean(SysMenuService.class);
        return menuService.getDirsByUserIdV2(userId);
    }
}
