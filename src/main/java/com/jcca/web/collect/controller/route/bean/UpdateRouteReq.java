package com.jcca.web.collect.controller.route.bean;

import lombok.Data;

import javax.validation.constraints.NotEmpty;

/**
 * 更新路由信息
 *
 * @author lyp
 */
@Data
public class UpdateRouteReq {

    /**
     * 记录ID
     */
    @NotEmpty(message = "更新的记录ID不可空")
    private String id;
    /**
     * 对端设备名称
     */
    private String atAssetName;
    /**
     * 对端端口名称
     */
    private String atPortName;
    /**
     * 备注信息
     */
    private String remark;


}
