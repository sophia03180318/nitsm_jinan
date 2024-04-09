package com.jcca.admin.system.validator;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author 小白龙
 * @date 2018/12/02
 */
@Data
public class OrgValid implements Serializable {
    @NotEmpty(message = "组织名称不能为空")
    private String title;
    @NotNull(message = "父级组织不能为空")
    private String pid;
    @NotNull(message = "组织类型不能为空")
    private Integer type;
}
