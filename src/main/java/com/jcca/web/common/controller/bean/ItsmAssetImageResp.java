package com.jcca.web.common.controller.bean;

import lombok.Data;

/**
 * @description: 设备型号
 * @author: Lvyp
 * @create: 2024/04/10 17:27
 */
@Data
public class ItsmAssetImageResp {

    /**
     * 资产型号
     * 183
     * 201
     * 42
     */
    private String assetMode;
    /**
     * 型号名称
     */
    private String name;
}
