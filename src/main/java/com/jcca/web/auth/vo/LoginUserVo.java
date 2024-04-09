package com.jcca.web.auth.vo;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @ClassName LoginUserVo
 * @Description 用于接收登录参数
 * @Date 2020/4/21 16:31
 * @Author hanwone
 */
@Data
public class LoginUserVo implements Serializable {
    @NotNull(message = "用户名不能为空")
    private String username;
    @NotNull(message = "密码不能为空")
    private String password;
}
