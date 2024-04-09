package com.jcca.web.statistics.service.bean;

import lombok.Data;

import java.io.Serializable;

/**
 * 设备运行时长
 */
@Data
public class AssetRunTime implements Serializable {
    /**
     * 设备名称
     */
    private String assetName;
    /**
     * 设备IP
     */
    private String assetIp;
    /**
     * 运行时长
     */
    private Long runTime;

}
