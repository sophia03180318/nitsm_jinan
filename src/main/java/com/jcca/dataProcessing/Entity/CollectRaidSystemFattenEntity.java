package com.jcca.dataProcessing.Entity;

import lombok.Data;

import java.util.List;

/**
 * Raid采集指标
 *
 * @author sophia
 */
@Data
public class CollectRaidSystemFattenEntity extends CommonEntity {

    /**
     * 磁盘总容量
     */
    private String totalCapacity;
    /**
     * 使用容量
     */
    private String usedCapacity;
    /**
     * Mdisk序列
     */
    private List<DiskEntity> mdiskList;

    /**
     * 池序列
     */
    private List<DiskEntity> groupList;

    /**
     * 卷序列
     */
    private List<DiskEntity> vdiskList;

    private String logInfo;



    private String collectCode;


    private List<String> logList;
    /**
     * driver
     */
    private List<DiskEntity> drives;
}
