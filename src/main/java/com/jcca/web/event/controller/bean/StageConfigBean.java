package com.jcca.web.event.controller.bean;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 阶段配置
 *
 * @author lyp
 */
@Data
public class StageConfigBean {

    /**
     * 验证排序
     */
    @NotNull(message = "验证排序不能空")
    private Integer sortNum;
    /**
     * 配置参数
     */
    @NotNull(message = "配置参数不能空")
    private Integer configNum;
    /**
     * 级别
     */
    @NotNull(message = "级别不能空")
    private Integer level;

}
