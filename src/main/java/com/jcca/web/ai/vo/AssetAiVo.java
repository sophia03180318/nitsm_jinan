package com.jcca.web.ai.vo;

import lombok.Data;

/**
 * @description: 设备请求
 * @author: sophia
 * @create: 2025/04/10 10:24
 **/
@Data
public class AssetAiVo {

    /**
     * 资产id
     */
    private String assetId;
    /**
     * 资产别名
     */
    private String name;
    /**
     * Ip地址
     */
    private String ip;

    /**
     * 设备类型(183：主机，42：路由器，201：交换机，263：oracle数据库)
     */
    private String assetMode;
    /**
     * 生产厂商ID
     */
    private String manufacturer;

    /**
     * 设备型号图片
     */
    private String assetImage;
    /**
     *
     */
    private String orgId;
    /**
     *
     */
    private String orgName;

}