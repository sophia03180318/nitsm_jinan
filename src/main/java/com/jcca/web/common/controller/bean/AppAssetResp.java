package com.jcca.web.common.controller.bean;

import com.jcca.web.collect.entity.CollectDisk;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * ITSM响应回的设备数据
 *
 * @author lyp
 */
@Data
public class AppAssetResp implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * 资产ID
     */
    private String id;
    /**
     * 组织ID
     */
    private String orgId;
    /**
     * 识别码
     */
    private String qrCodeNum;
    /**
     * 机房ID
     */
    private String roomId;
    /**
     * 机柜ID
     */
    private String cabinetId;
    /**
     * 设备名称
     */
    private String name;
    /**
     * 设备IP
     */
    private String ip;
    /**
     * 设备IP2
     */
    private String ip2;
    /**
     * 登录用户名
     */
    private String userName;
    /**
     * 登录密码
     */
    private String pwd;
    /**
     * 团体名
     */
    private String snmpUser;
    /**
     * 登录端口
     */
    private String loginPort;
    /**
     * 资产供货商
     */
    private String assetSupplier;
    /**
     * 厂商
     */
    private String manufacturer;
    /**
     * 序列号
     */
    private String serialNumber;
    /**
     * 资产类型
     */
    private Integer assetMode;
    /**
     * 资产型号
     */
    private String assetImage;
    /**
     * 系统类型
     */
    private Integer systemType;

    private String roomName;

    private String orgName;

    private String cabName;

    /**
     * 资产在机柜中起始位置
     */
    private Integer startPosition;
    /**
     * 资产在机柜中结束位置
     */
    private Integer endPosition;
    /**
     * 上架时间
     */
    private String onlineTime;
    /**
     * 下架时间
     */
    private String downlineTime;
    /**
     * 主从节点
     */
    private String masterOrSlave;
    /**
     * cpu使用率
     */
    private List<CpuResp> cpuData;
    /**
     * 内存使用率
     */
    private List<MemoryResp> memoryData;
    /**
     * 磁盘信息
     */
    private List<CollectDisk> diskData;

    /**
     * 是否核心设备，SHOW_TOPO_@_NO_SHOW 不是，SHOW_TOPO_@_SHOW是
     */
    private String showCore;


}
