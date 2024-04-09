package com.jcca.web2.vo;

import lombok.Data;

/**
 * @description: 资产详情界面设备基础通用信息
 * @author: Lvyp
 * @create: 2023/11/02 14:05
 */
@Data
public class AssetBaseInfoVo {

    /**
     * 设备ID
     */
    private String id;
    /**
     * 组织ID
     */
    private String orgId;
    /**
     * 设备名称
     */
    private String name;
    /**
     * 资产编号
     */
    private String assetCode;
    /**
     * 设备类型(183：主机，42：路由器，201：交换机，263：oracle数据库,318存储)
     */
    private Integer assetMode;
    /**
     * 设备所在机房名字
     */
    private String roomName;
    /**
     * 设备所在机柜名字
     */
    private String cabinetName;
    /**
     * 生产厂商ID
     */
    private Integer manufacturerId;
    /**
     * 生产厂商名字
     */
    private String manufacturerName;
    /**
     * 操作系统名字
     */
    private String operationSystem;
    /**
     * 操作系统版本
     */
    private String assetVersion;
    /**
     * 资产型号
     */
    private String assetImage;
    /**
     * 资产型号图片
     */
    private Integer assetImagePath;
    /**
     * 设备IP1
     */
    private String ip1;
    /**
     * 设备IP2
     */
    private String ip2;

}
