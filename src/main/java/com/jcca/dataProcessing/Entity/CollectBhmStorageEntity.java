package com.jcca.dataProcessing.Entity;


import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 采集管理口的CPU信息
 */
@Data
public class CollectBhmStorageEntity extends CommonEntity implements Serializable {

    private String id;

    private String name;

    /**
     * RAID组内的硬盘列表
     */
    private List<ReadFishDiskEntity> diskInfos;
    /**
     * 存储控制器列表
     */
    private List<ReadFishStorageControllersEntity> storageControllers;

    private Integer count;
}
