package com.jcca.web.asset.controller.bean;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class AssetExportBean implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 资产id
     */
    private String id;
    /**
     * 资产别名
     */
    private String name;
    /**
     * 资产编号
     */
    private String assetCode;
    /**
     * 20210112hanwone 用于区分设备类型的细分，在原服务器183后面追加数字1，2，3 1终端，2小型机，3工控机
     * 最终存为1831，1832，1833 原设备类型不变存入该字段
     */
    private String desk;
    /**
     * 调度台 主机编号
     */
    private String hostNumber;
    /**
     * 调度台 连接显示器数量
     */
    private String displayerTotal;
    /**
     * 调度台 视频接口类型
     */
    private String displayerPortModel;
    /**
     * Ip地址
     */
    private String ip;
    /**
     * 设备登录账号
     */
    private String osUser;
    /**
     * 设备登录密码
     */
    private String osPassword;
    /**
     * 设备类型(183：主机，42：路由器，201：交换机，263：oracle数据库)
     */
    private String assetMode;
    /**
     * 设备状态(0：停用，1：启用)
     */
    private String status;
    /**
     * 序列号
     */
    private String serialNumber;
    /**
     * 运行模式 AssetRunModelEnum
     */
    private String runModel;
    /**
     * 电源模块型号
     */
    private String powerModel;
    /**
     * 电源模块数量
     */
    private Integer powerTotal;
    /**
     * 内存
     */
    private String memory;
    /**
     * 磁盘数量
     */
    private Integer diskTotal;
    /**
     * 单磁盘容量
     */
    private String diskCapacity;
    /**
     * 生产厂商ID
     */
    private String manufacturerId;
    /**
     * 资产供货商 ManufacturersEnum
     */
    private String assetSupplier;
    /**
     * 组织ID
     */
    private String orgId;
    /**
     * 操作系统名称
     */
    private String operationSystem;
    /**
     * 管理口IP
     */
    private String ipmiIp;
    /**
     * 上架时间
     */
    private Date onlineTime;
    /**
     * 下架时间
     */
    private Date downlineTime;
    /**
     * 跳转路径
     */
    private String skipUrl;
    /**
     * 采集操作系统类型
     */
    private String collectionType;
    /***
     * 采集协议
     */
    private String protocolType;
    /**
     * 采集协议
     */
    private String protocolTypeStr;
    /**
     * 主备信息(主:1,被:0)
     */
    private String initative;
    /**
     * 设备图片
     */
    private String assetImage;
    /**
     * CPU个数
     */
    private Integer cpuNumber;
    /**
     * CPU核数
     */
    private Integer cpuCoreNumber;
    /**
     * CPU型号
     */
    private String cpuModel;
    /**
     * CPU主频
     */
    private String cpuFrequency;
    /**
     * Ip地址
     */
    private String ip2;
    /**
     * 数据状态 1-正常，3已删除
     */
    private String isDel;
    /**
     * 是否显示在拓扑图中 0-不显示，1-显示
     */
    private String showTopo;
    /**
     * 是否开启ntp采集 0-不采集，1-采集
     */
    private String ntpFlag;
    /**
     * 设备是否监控 0-不监控，1-监控
     */
    private String watch;
    /**
     * 质保期限
     */
    private String validityDate;
    /**
     * 创建时间
     */
    private String createTime;
    /**
     * 创建者
     */
    private String creator;
    /**
     * 修改时间
     */
    private String modifyTime;
    /**
     * 修改者
     */
    private String modifier;

    /**
     * 机房ID
     */
    private String roomId;
    /**
     * 机柜ID
     */
    private String cabinetId;
    /**
     * 资产在机柜中起始位置
     */
    private Integer startPosition;
    /**
     * 资产在机柜中结束位置
     */
    private Integer endPosition;
    /**
     * 资产阈值设置类型 ThresholdAutoFlagEnum
     */
    private String autoFlag;
    /**
     * 端口
     */
    private Integer port;
    /**
     *
     */
    private String abflag;
    /**
     * 资产位置
     */
    private String assetSite;

}
