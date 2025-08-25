package com.jcca.dataProcessing.Entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author: hhw
 * @description: CollectHardwareBean 主要是用来 存储硬件信息
 * @date: 2025-08-25  13:08
 * @since: 2.1.9.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class CollectHardwareBean extends CommonEntity {

    /**
     * 主机名
     */
    private String hostName;
    /**
     * 操作系统版本
     */
    private String osVersion;
    /**
     * 主机厂商
     */
    private String hostManufacture;
    /**
     * cpu物理核心数
     */
    private String cpuNum;
    /**
     * cpu型号
     */
    private String cpuModel;
    /**
     * cpu频率
     */
    private String cpuMHz;
    /**
     * cpu最大频率
     */
    private String cpuMaxMHz;
    /**
     * cpu最小频率
     */
    private String cpuMinMHz;
}
