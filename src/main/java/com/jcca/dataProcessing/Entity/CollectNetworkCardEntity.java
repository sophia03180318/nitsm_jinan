package com.jcca.dataProcessing.Entity;

import lombok.Data;

import java.io.Serializable;

/**
 * 采集网卡信息
 *
 * @author Lvyp
 */
@Data
public class CollectNetworkCardEntity extends CommonEntity implements Serializable {

    private static final long serialVersionUID = 1L;


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
     * 端口流入量
     */
    private Long portIn;
    /**
     * 端口流出量
     */
    private Long portOut;
    /**
     * 端口接收率
     */
    private Long portInSpeed;
    /**
     * 端口发送率
     */
    private Long portOutSpeed;
    /**
     * mac地址
     */
    private String macAddress;
    /**
     * ip地址
     */
    private String ip;

    private String collectCode;
}
