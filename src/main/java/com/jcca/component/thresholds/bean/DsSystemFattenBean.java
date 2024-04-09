package com.jcca.component.thresholds.bean;

import lombok.Data;

import java.util.List;

/**
 * DS采集指标
 *
 * @author sophia
 */
@Data
public class DsSystemFattenBean {

    private String assetId;


    /**
     * 容量大小
     */
    private Long capacity;

    /**
     * 剩余容量
     */
    private Long freeCapacity;

    /**
     * controller
     */
    private List<DSBean> controllers;

    /**
     * arrays
     */
    private List<DSBean> arrays;

    /**
     * logicalDriver
     */
    private List<DSBean> logicalDrives;

    private List<DSBean> logicalDrives2;

    /**
     * driver
     */
    private List<DSBean> drives;


    private List<String> logInfo;

    /**
     * 采集时间
     */
    private String collectTime;


}
