package com.jcca.dataProcessing.Entity;

import lombok.Data;

import java.util.List;

/**
 * DS采集指标
 *
 * @author sophia
 */
@Data
public class DsSystemFattenEntity extends CommonEntity {


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
    private List<DSEntity> controllers;

    /**
     * arrays
     */
    private List<DSEntity> arrays;

    /**
     * logicalDriver
     */
    private List<DSEntity> logicalDrives;

    private List<DSEntity> logicalDrives2;

    /**
     * driver
     */
    private List<DSEntity> drives;


    private List<String> logInfo;


}
