package com.jcca.web.asset.detail.bean;

import lombok.Data;

import java.util.Date;

/**
 * @ClassName DetailInterfaces
 * @Description 资产详情----面板端口信息
 * @Date 2020/6/23 13:30
 * @Author hanwone
 */
@Data
public class DetailInterfaces {

    /**
     * 采集时间
     */
    private Date collectTime;
    /**
     * 资产ID
     */
    private String assetId;
    /**
     * 资产IP
     */
    private String assetIp;
    /**
     * 端口名称
     */
    private String portName;
    /**
     * 端口别名
     */
    private String portAlias;
    /**
     * 端口类型
     */
    private Integer portType;
    /**
     * 端口索引
     */
    private String portIndex;
    /**
     * 端口连接类型1:half,2:full,3:disagree,4:auto
     */
    private Integer portLinkType;
    /**
     * 端口对应的IP地址
     */
    private String linkIp;
    /**
     * 端口对应网络的mask
     */
    private String linkMask;
    /**
     * 物理地址
     */
    private String linkPhyAddress;
    /**
     * 端口流入量
     */
    private Long portIn;
    /**
     * 端口流出量
     */
    private Long portOut;
    /**
     * 端口流入丢包数
     */
    private Long discardPacketsIn;
    /**
     * 端口流出丢包数
     */
    private Long discardPacketsOut;
    /**
     * 非单播流入量
     */
    private Long noUnicastPacketsIn;
    /**
     * 非单播流出量
     */
    private Long noUnicastPacketsOut;
    /**
     * 单播流入量
     */
    private Long unicastPacketsIn;
    /**
     * 单播流出量
     */
    private Long unicastPacketsOut;
    /**
     * 误码流入量
     */
    private Long errorCodeIn;
    /**
     * 误码流出量
     */
    private Long errorCodeOut;
    /**
     * 端口状态1：启用，2:空闲
     */
    private Byte status;
    /**
     * 采集状态
     * 1通/0断
     * StatusEnum
     */
    private Byte collectStatus;
    /**
     * 端口速率
     */
    private String portSpeed;
    /**
     * 端口接收率
     */
    private String portRateIn;
    /**
     * 端口发送率
     */
    private String portRateOut;
    /**
     * 端口发送丢包率
     */
    private String losePacketsRateOut;
    /**
     * 端口接收丢包率
     */
    private String losePacketsRateIn;
    /**
     * 端口发送误码率
     */
    private String sendCodeRateOut;
    /**
     * 端口接收误码率
     */
    private String erroCodeRateIn;
    private Long crcErrors;
}
