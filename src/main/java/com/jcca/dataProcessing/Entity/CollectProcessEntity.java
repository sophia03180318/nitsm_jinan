package com.jcca.dataProcessing.Entity;

import lombok.Data;

/**
 * 阈值-进程采集
 *
 * @author Lvyp
 */
@Data
public class CollectProcessEntity extends  CommonEntity  {

    private static final long serialVersionUID = 1L;

    /**
     * 进程名称
     */
    private String name;
    /**
     * 进程别名
     */
    private String alias;
    /**
     * 进程ID
     */
    private String processId;
    /**
     * cpu使用率
     */
    private Double cpuRate;
    /**
     * 内存使用率
     */
    private Double memoryRate;

    private Boolean status;
    /**
     * 进程在阈值表的ID
     */
    private String thresholdId;
    /**
     * 是否是车站进程
     */
    private Boolean stationAsset;

}
