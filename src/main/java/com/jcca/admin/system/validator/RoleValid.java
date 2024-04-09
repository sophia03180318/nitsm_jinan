package com.jcca.admin.system.validator;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;

/**
 * @author 小白龙
 * @date 2018/8/14
 */
@Data
public class RoleValid implements Serializable {
    @NotEmpty(message = "角色标识不能为空")
    private String name;
    @NotEmpty(message = "角色名称不能为空")
    private String title;
    @NotEmpty(message = "父级角色不能为空")
    private String pid;
}
