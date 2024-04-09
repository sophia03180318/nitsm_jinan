package com.jcca.web2.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jcca.web.auth.controller.bean.LoginConfBean;
import com.jcca.web2.entity.SysLoginConf;

/**
 * @author HanHW
 * @description 用户登录控制
 * @className SysLoginConfService
 * @date 2023/10/26 10:14
 * @since 2.1.0.0
 */
public interface SysLoginConfService extends IService<SysLoginConf> {
    /**
     * @description: 设置登录配置
     * @author: HanHW
     * @date: 2023/10/26 10:59
     * @param: [loginConfBean]
     * @return: void
     **/
    void setLoginConf(LoginConfBean loginConfBean);
}
