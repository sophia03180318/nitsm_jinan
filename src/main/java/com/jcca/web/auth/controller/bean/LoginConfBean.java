package com.jcca.web.auth.controller.bean;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * 安全配置BEAN
 *
 * @author lyp
 */
@Data
public class LoginConfBean {

    @NotEmpty(message = "用户ID不能为空")
    private String userId;

    /**
     * 登录用户名
     */
    private String username;
    /**
     * 允许访问的IP列表
     */
    private List<String> ips;
    /**
     * 最后一次登录时间
     */
    private String lastTime;
    /**
     * 登录次数
     */
    private Integer trySize;
    /**
     * 最大值
     */
    private Integer maxSize = 5;
    /**
     * 锁定时间,单位分钟
     */
    private Integer lockTime;
}
