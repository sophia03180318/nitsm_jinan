package com.jcca.web.asset.controller.bean;

import lombok.Data;

import javax.validation.constraints.Digits;
import javax.validation.constraints.NotEmpty;

/**
 * @author: hhw
 * @description: AssetAppServerReq 主要是用来
 * @date: 2025-07-04  13:58
 * @since: 2.0.15.0
 */
@Data
public class AssetAppServerReq {

    @NotEmpty(message = "资产ID不能为空")
    private String assetId;
    @NotEmpty(message = "服务器端口不能为空")
    private String serverPort;
    @Digits(integer = 2, fraction = 2, message = "CPU负载只能输入2位整数最多两位小数")
    private Double cpuLoad;
}
