package com.jcca.web2.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * @author HanHW
 * @description 给角色授权组织
 * @className AuthOrgDto
 * @date 2023/10/20 11:31
 * @since 2.1.0.0
 */
@Data
public class AuthOrgDto {

    @NotEmpty(message = "角色ID不能为空")
    private String id;

    private List<String> orgIds;
}
