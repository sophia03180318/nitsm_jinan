package com.jcca.web.auth.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.jcca.admin.system.service.SysUserService;
import com.jcca.common.bean.constant.AdminConst;
import com.jcca.common.enums.ResultEnum;
import com.jcca.common.enums.StatusEnum;
import com.jcca.common.exception.ResultException;
import com.jcca.web.auth.service.LoginSecurityService;
import com.jcca.web2.entity.SysLoginConf;
import com.jcca.web2.service.SysLoginConfService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Objects;

/**
 * 账号安全策略验证实现
 *
 * @author lyp
 */
@Service
public class LoginSecurityServiceImpl implements LoginSecurityService {

    @Resource
    private SysLoginConfService loginConfService;
    @Resource
    private SysUserService userService;

    @Override
    public void securityVerify(String userId, String remoteAddr) {
        if (AdminConst.ADMIN_ID.equals(userId)) {
            return;
        }

        SysLoginConf loginConf = loginConfService.getById(userId);
        if (Objects.isNull(loginConf)) {
            return;
        }

        //校验IP
        String ipList = loginConf.getIpList();
        boolean b = !("127.0.0.1".equals(remoteAddr) || "localhost".equals(remoteAddr) || "0:0:0:0:0:0:0:1".equals(remoteAddr));
        if (b) {
            if (!StringUtils.isEmpty(ipList) && !ipList.contains(remoteAddr)) {
                throw new ResultException(ResultEnum.ILLEGAL_IP.getCode(), ResultEnum.ILLEGAL_IP.getMessage() + "：" + remoteAddr);
            }
        }

        Integer maxSize = loginConf.getMaxSize();
        Integer trySize = loginConf.getTrySize();
        String lastTime = loginConf.getLastTime();

        if (maxSize.intValue() == trySize.intValue() || maxSize < trySize) {
            //超过最大尝试次数
            if (StrUtil.isEmpty(lastTime)) {
                loginConf.setLastTime(DateUtil.now());
            }
            Integer lockTime = loginConf.getLockTime();
            Date lastDate = DateUtil.parseDateTime(lastTime).toJdkDate();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(lastDate);
            calendar.add(Calendar.MINUTE, lockTime);
            long now = System.currentTimeMillis();
            long l = calendar.getTime().getTime() - now;

            if (l > 0) {
                loginConf.setStatus(StatusEnum.FREEZED.getCode());
                loginConfService.updateById(loginConf);

                // 锁定时修改用户状态为 2:冻结
                userService.updateStatus(StatusEnum.FREEZED.name(), Collections.singletonList(userId));

                throw new ResultException(ResultEnum.ACCOUNT_FREEZED.getCode(), "账号已被锁定，请" + (l > 60000 ? ((l / 60000) + "分钟后再尝试") : "1分钟后再尝试"));
            }
        }
    }


    /**
     * 获取请求者IP地址
     *
     * @param request
     * @return
     */
    public static String getIpAddr(HttpServletRequest request) {
        String ipAddress = null;
        try {
            ipAddress = request.getHeader("x-forwarded-for");
            if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
                ipAddress = request.getHeader("Proxy-Client-IP");
            }
            if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
                ipAddress = request.getHeader("WL-Proxy-Client-IP");
            }
            if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
                ipAddress = request.getRemoteAddr();
                if (ipAddress.equals("127.0.0.1")) {
                    // 根据网卡取本机配置的IP
                    InetAddress inet = null;
                    try {
                        inet = InetAddress.getLocalHost();
                    } catch (UnknownHostException e) {
                        throw new UnknownHostException("获取请求者IP地址异常-URL:" + request.getRequestURL());
                    }
                    ipAddress = inet.getHostAddress();
                }
            }
            // 对于通过多个代理的情况，第一个IP为客户端真实IP,多个IP按照','分割
            if (ipAddress != null && ipAddress.length() > 15) {
                // "***.***.***.***".length()
                // = 15
                if (ipAddress.indexOf(",") > 0) {
                    ipAddress = ipAddress.substring(0, ipAddress.indexOf(","));
                }
            }
        } catch (Exception e) {
            ipAddress = "";
        }
        return ipAddress;
    }

}
