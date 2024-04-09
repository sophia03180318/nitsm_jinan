package com.jcca.component.thresholds.bean;

import lombok.Data;

import java.io.Serializable;

/**
 * 车站时间
 *
 * @author Lvyp
 */
@Data
public class CollectStationSystemTimeBean implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 资产主键
     */
    private String assetId;
    /**
     * 采集编号
     */
    private String collectCode;
    /***
     * 采集时间 yyyyMMddHHmmss
     */
    private String collectTime;
    /***
     * 被采集设备时间 yyyyMMddHHmmss
     */
    private String remoteTime;
    /***
     * 采集器设备时间 yyyyMMddHHmmss
     */
    private String localTime;
    /***
     * 时间偏差毫秒
     */
    private String timeSpan;
    /**
     * 设备运行时长
     */
    private Long timeduration;

}
