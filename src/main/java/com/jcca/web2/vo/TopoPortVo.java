package com.jcca.web2.vo;

import lombok.Data;

/**
 * @description: topo用端口
 * @author: sophia
 * @create: 2025/01/17 11:36
 */
@Data
public class TopoPortVo {

    /**
     * 网卡名称、端口名称
     */
    private String name;

    /**
     * 网卡\端口 状态
     * 0断1通2未知
     *
     */
    private Integer status;

    /**
     * 虚拟资产此项为空
     */
    private String assetId;


}
