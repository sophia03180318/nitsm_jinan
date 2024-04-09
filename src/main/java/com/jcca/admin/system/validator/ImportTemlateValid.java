package com.jcca.admin.system.validator;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;

/**
 * @ Author：sophia
 * @ Date：Created in 17:51 2021/6/25
 * @ Description:
 */
@Data
public class ImportTemlateValid implements Serializable {
    @NotEmpty(message = "模板名不能重复")
    private String name;
    private String nickname;
    private String confirm;
}
