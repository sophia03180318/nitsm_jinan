package com.jcca.component.thresholds.bean;

import lombok.Data;

import java.io.Serializable;

/**
 * 网卡通断content
 *
 * @author Lvyp
 */
@Data
public class ContentNetworkCard implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 网卡名称
     */
    private String name;
    /**
     * 告警编号
     */
    private String alarmCode;

}
