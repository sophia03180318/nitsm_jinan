package com.jcca.web2.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;

/**
 * @author HanHW
 * @description 配置进程
 * @className ConfigProcessDto
 * @date 2023/11/13 17:36
 * @since 2.1.0.0
 */
@Data
public class ConfigProcessDto {

    /**
     * 资产ID
     */
    @NotEmpty(message = "资产ID不能为空")
    private String assetId;
    /**
     * 进程配置名称
     */
    @NotEmpty(message = "进程名称不能为空")
    private String processName;
    /**
     * 进程ID
     */
    @NotEmpty(message = "进程ID不能为空")
    private String processId;
    /**
     * cpu阈值
     */
    private String thresholdCpu;
    /**
     * 内存阈值
     */
    private String thresholdMemory;

    private String remark;
}
