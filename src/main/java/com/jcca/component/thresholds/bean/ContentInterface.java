package com.jcca.component.thresholds.bean;

import lombok.Data;

/**
 * 硬件接口告警Content
 *
 * @author Lvyp
 */
@Data
public class ContentInterface {
    /**
     * 接口索引
     */
    private String interfaceIndex;
    /**
     * 接口名称
     */
    private String interfaceName;
    /**
     * 告警编号
     */
    private String alarmCode;

}
