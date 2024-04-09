package com.jcca.component.client.bean;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import java.io.Serializable;

/**
 * 采集资产进程
 *
 * @author Lvyp
 */
@Data
public class CollectProcessReq implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 资产ID
     */
    @NotEmpty(message = "资产ID不能空")
    private String assetId;
    /**
     * 是否是车站
     */
    @NotEmpty(message = "是否为车站不可空")
    @Pattern(regexp = "[1-2]", message = "是否为车站不可空")
    private String isStation;
    /**
     * 车站IP
     */
    private String stationIp;
    /**
     * 车站端口
     */
    private Integer stationPort;

}
