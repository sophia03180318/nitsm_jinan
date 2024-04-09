package com.jcca.web.statistics.service.bean;

import lombok.Data;

import java.io.Serializable;

/**
 * 资产状态统计
 */
@Data
public class AssetStatusStatistics implements Serializable {

    /**
     * 类型名称
     */
    private String moldName;
    /**
     * 该类型的设备总数
     */
    private Integer moldAssetCount;
    /**
     * 正常状态
     */
    private Integer normalCount;
    /**
     * 异常状态
     */
    private Integer abnormalCount;
    /**
     * 不监控
     */
    private Integer unmonitoredCount;

}
