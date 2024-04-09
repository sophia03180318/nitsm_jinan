package com.jcca.dataProcessing.Entity;

import lombok.Data;

import java.io.Serializable;

/**
 * 车站时间
 *
 * @author Lvyp
 */
@Data
public class CollectStationSystemTimeEntity extends CommonEntity implements Serializable {

    private static final long serialVersionUID = 1L;


    /**
     * 采集编号
     */
    private String collectCode;

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
    private Long timeSpan;
    /**
     * 设备运行时长
     */
    private Long timeduration;

}
