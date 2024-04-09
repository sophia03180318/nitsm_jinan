package com.jcca.web.asset.vo;

import lombok.Data;

import java.util.Date;

/**
 * 资产详细信息
 *
 * @author lyp
 */
@Data
public class AssetMsgVo {

    private String id;
    private String assetName;

    private String assetImage;
    private String serialNumber;
    private String operationSystem;
    private String assetSupplier;
    private String assetVersion;
    private String remark;
    /**
     * 资产ip
     */
    private String ip;
    /**
     * 资产编号
     */
    private String assetCode;
    /**
     * 设备类型
     */
    private String assetMode;
    /**
     * 组织结构
     */
    private String orgName;
    /**
     * 机房名称
     */
    private String roomName;
    /**
     * 上架时间
     */
    private Date onlineTime;
    /**
     * 下架时间
     */
    private Date downlineTime;
    /**
     * 生产厂商名称
     */
    private String manufacturerName;
    /**
     * 机柜编号
     */
    private String cabinetCode;
    /**
     * 机柜名称
     */
    private String cabinetName;
    /**
     * 距离下线的时间
     * 毫秒
     */
    private Long downlineTimeCount;
    /**
     * xx天xx小时xx分
     */
    private String downlineTimeCountStr;

    /**
     * U位位置
     */
    private String positionStr;
}
