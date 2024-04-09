package com.jcca.component.thresholds.bean;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 采集网卡信息
 *
 * @author Lvyp
 */
@Data
public class CollectNetworkCardBean implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 采集时间
     */
    @NotEmpty(message = "采集时间空")
    private String collectTime;
    /**
     * 资产ID
     */
    @NotEmpty(message = "资产ID空")
    private String assetId;
    /**
     * 系统类型 SystemTypeEnum
     */
    @NotNull(message = "系统类型空")
    private Integer systemType;
    /**
     * 网卡名称
     */
    @NotEmpty(message = "网卡名称空")
    private String name;
    /**
     * 网卡状态 CollectNetCardStatus
     */
    @NotNull(message = "网卡状态空")
    private Byte status;
    /**
     * 端口流入量
     */
    @NotNull(message = "端口流入量空")
    private Long portIn;
    /**
     * 端口流出量
     */
    @NotNull(message = "端口流出量空")
    private Long portOut;
    /**
     * 端口接收率
     */
    @NotNull(message = "端口接收率空")
    private Long portInSpeed;
    /**
     * 端口发送率
     */
    @NotNull(message = "端口发送率空")
    private Long portOutSpeed;
    /**
     * mac地址
     */
    @NotEmpty(message = "MAC地址空")
    private String macAddress;
    /**
     * ip地址
     */
    @NotEmpty(message = "IP空")
    private String ip;

}
