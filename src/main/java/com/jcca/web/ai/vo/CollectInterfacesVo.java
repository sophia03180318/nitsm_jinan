package com.jcca.web.ai.vo;

import lombok.Data;

import java.util.Date;

/**
 * 硬件端口
 *
 * @author Lvyp
 */
@Data
public class CollectInterfacesVo {

    private String id;

    /**
     * 采集时间
     */
    private Date collectTime;
    /**
     * 资产主键
     */
    private String assetId;
    /**
     * 端口名称
     */
    private String portName;

    /**
     * 端口类型
     */
    private Integer portType;
    /**
     * 端口索引
     */
    private String portIndex;
    /**
     * 端口对应的IP地址
     */
    private String linkIp;
    /**
     * 端口对应的mask地址
     */
    private String linkMask;

    /**
     * 端口状态 InterfaceStatus
     */
    private Byte status;
    /**
     * 端口接收速率
     */
    private Long portInSpeed = 0L;
    /**
     * 端口发送速率
     */
    private Long portOutSpeed = 0L;

    private String peerPort;

    private String peerIp;

    private String peerDevice;


}
