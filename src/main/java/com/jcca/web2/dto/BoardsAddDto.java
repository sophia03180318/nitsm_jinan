package com.jcca.web2.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;

/**
 * @description: 添加图标模板
 * @author: Lvyp
 * @create: 2023/11/10 17:39
 */
@Data
public class BoardsAddDto {

    @NotEmpty(message = "请输入模板名称")
    private String name;

    private String configs;

    private String tags;
    /**
     * 可空，如非空则关联至资产详情
     */
    private Integer assetMode;
}
