package com.jcca.web2.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcca.web.auth.controller.bean.LoginConfBean;
import com.jcca.web2.dao.SysLoginConfMapper;
import com.jcca.web2.entity.SysLoginConf;
import com.jcca.web2.service.SysLoginConfService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * @author HanHW
 * @description 用户登录控制
 * @className SysLoginConfServiceImpl
 * @date 2023/10/26 10:14
 * @since 2.1.0.0
 */
@Service
public class SysLoginConfServiceImpl extends ServiceImpl<SysLoginConfMapper, SysLoginConf> implements SysLoginConfService {
    /**
     * @param loginConfBean
     * @description: 设置登录配置
     * @author: HanHW
     * @date: 2023/10/26 10:59
     * @param: [loginConfBean]
     * @return: void
     */
    @Override
    public void setLoginConf(LoginConfBean loginConfBean) {
        Integer maxSize = loginConfBean.getMaxSize();
        Integer lockTime = loginConfBean.getLockTime();
        Integer trySize = loginConfBean.getTrySize();
        if (Objects.isNull(maxSize) || maxSize <= 0 || maxSize > 5) {
            loginConfBean.setMaxSize(5);
        }
        if (Objects.isNull(lockTime) || lockTime <= 0) {
            loginConfBean.setLockTime(5);
        }
        if (Objects.isNull(trySize) || trySize <= 0) {
            loginConfBean.setTrySize(0);
        }
        List<String> ips = loginConfBean.getIps();
        String userId = loginConfBean.getUserId();
        SysLoginConf loginConf = this.getById(userId);
        if (Objects.isNull(loginConf)) {
            loginConf = new SysLoginConf();
            BeanUtils.copyProperties(loginConfBean, loginConf);
            loginConf.setIpList("");
            if (!CollectionUtils.isEmpty(ips)) {
                loginConf.setIpList(JSONUtil.toJsonStr(ips));
            }

            loginConf.setCreateTime(new Date());
            this.save(loginConf);
            return;
        }

        BeanUtils.copyProperties(loginConfBean, loginConf);
        loginConf.setIpList("");
        if (!CollectionUtils.isEmpty(ips)) {
            loginConf.setIpList(JSONUtil.toJsonStr(ips));
        }
        loginConf.setModifyTime(new Date());
        this.saveOrUpdate(loginConf);
    }
}
