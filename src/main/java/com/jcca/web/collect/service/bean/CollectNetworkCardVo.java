package com.jcca.web.collect.service.bean;

import lombok.Data;

import java.util.Date;

/**
 * 采集网卡信息
 */
@Data
public class CollectNetworkCardVo {
    /**
     * 设备名称
     */
    private String assetName;
    /**
     * 设备ip
     */
    private String assetIp;

    private String id;
    /**
     * 采集时间
     */

    private Date collectTime;
    /**
     * 采集编号 同一台设备同一次采集编号相同
     */
    private String collectCode;
    /**
     * 资产ID
     */
    private String assetId;
    /**
     * 系统类型 SystemTypeEnum
     */
    private Integer systemType;
    /**
     * 网卡名称
     */
    private String name;
    /**
     * 网卡状态 CollectNetCardStatus
     */
    private Byte status;
    /**
     * mac地址
     */
    private String macAddress;
    /**
     * ip地址
     */
    private String ip;
    /**
     * 端口流出
     */
    private Long portOut;
    /**
     * 端口流入
     */
    private Long portIn;
    /**
     * 端口流出率
     */
    private Long portOutSpeed;
    /**
     * 端口流入率
     */
    private Long portInSpeed;
    /**
     * 创建时间
     */
    private Date createTime;

}
