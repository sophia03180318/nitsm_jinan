package com.jcca.admin.system.validator;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;

/**
 * @author 小白龙
 * @date 2018/8/14
 */
@Data
public class UserValid implements Serializable {
    @NotEmpty(message = "用户名不能为空")
    private String username;
    @NotEmpty(message = "用户昵称不能为空")
    private String nickname;
    private String confirm;
}
