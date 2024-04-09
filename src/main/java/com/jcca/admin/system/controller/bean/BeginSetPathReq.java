package com.jcca.admin.system.controller.bean;

import lombok.Data;

import javax.validation.constraints.NotEmpty;

/**
 * 设置路径
 *
 * @author lyp
 */
@Data
public class BeginSetPathReq {
    /**
     * 保存地址
     */
    @NotEmpty(message = "保存地址不能空")
    private String savePath;
    /**
     * 车站ID
     */
    @NotEmpty(message = "车站ID不能空")
    private String setPathStationId;
}
