package com.jcca.component.thresholds.bean;

import lombok.Data;

import java.util.List;

/**
 * Raid采集指标
 *
 * @author sophia
 */
@Data
public class CollectRaidSystemFattenBean {

    private String assetId;
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
    private List<DiskBean> mdiskList;

    /**
     * 池序列
     */
    private List<DiskBean> groupList;

    /**
     * 卷序列
     */
    private List<DiskBean> vdiskList;

    private String logInfo;

    /**
     * 采集时间
     */

    private String collectTime;

    private String collectCode;


    private List<String> logList;
    /**
     * driver
     */
    private List<DiskBean> drives;
}
