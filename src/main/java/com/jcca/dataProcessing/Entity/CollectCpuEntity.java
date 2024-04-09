package com.jcca.dataProcessing.Entity;

import lombok.Data;

import java.io.Serializable;
import java.util.List;


@Data
public class CollectCpuEntity extends CommonEntity implements Serializable {

    private static final long serialVersionUID = 1L;


    /**
     * cpu使用率
     */
    private Double cpuUsedRate;
    /**
     * 设定阈值
     */
   // private ThresholdBaseEntity threshold;

    /**
     * cpu标识 一台设备多个cpu的时候 此参数要唯一
     */
    private String cpuFlg;

    /**
     * 进程CPU占用率较大前5
     */
    private List<CollectProcessEntity> processTop5List;
}
