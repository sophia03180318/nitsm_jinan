package com.jcca.component.thresholds.bean;

import lombok.Data;

import java.io.Serializable;

/**
 * 环境检测传感检测采集信息
 */
@Data
public class TSensor implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 采集编号 同一台设备同一次采集编号相同
     */
    private String collectCode;
    /**
     * 资产ID
     */
    private String assetId;
    /**
     * 序列号
     */
    private String serialNumberName;
    /**
     * 传感类型
     */
    private String sensorType;
    /**
     * 采集到的值
     */
    private String value;
    /**
     * 状态
     * 0 未开启 1 正常 2 告警异常
     */
    private Integer status;
    /**
     * 描述信息
     */
    private String descStr;
    /***
     * 采集时间 时间戳
     */
    private String collectTime;
}
