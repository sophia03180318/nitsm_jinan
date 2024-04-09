package com.jcca.admin.system.validator;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;

/**
 * @author 小白龙
 * @date 2018/10/19
 */
@Data
public class ActionLogValid implements Serializable {
    @NotEmpty(message = "标题不能为空")
    private String title;
}
