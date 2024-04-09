package com.jcca.web2.vo;

import lombok.Data;

/**
 * 设备性能数据查询结果
 *
 * @description: 设备性能数据查询结果
 * @author: Lvyp
 * @create: 2023/12/25 15:33
 */
@Data
public class AssetPerformanceDataVo {

    /**
     * 资产ID
     */
    private String assetId;
    /**
     * 设备状态(0：离线，1：在线，2：不监控,4:未知)
     */
    private Byte status;
    /**
     * CPU使用率
     */
    private Double cpuUsedRate;
    /**
     * 内存使用率
     */
    private Double memUsedRate;
    /**
     * 交换内存使用率
     */
    private Double switchMemUsedRate;
    /**
     * 内存总量
     * 带单位的字符串
     */
    private String memoryTotal;
    /**
     * 内存使用量
     * 带单位的字符串
     */
    private String memoryUsed;
    /**
     * 交换内存总量
     * 带单位的字符串
     */
    private String switchMemTotal;
    /**
     * 交换内存使用量
     * 带单位的字符串
     */
    private String switchMemUsed;
    /**
     * 运行时长
     *
     */
    private Long runTime;
    /**
     * 偏差时长  单位秒
     */
    private Long deviationTime;
    /**
     * 磁盘阵列总容量
     * 带单位的字符串
     */
    private String raidTotal;
    /**
     * 磁盘阵列已配置容量带单位的字符
     */
    private String raidConfig;
    /**
     * 磁盘阵列可配置容量 带单位的字符
     */
    private String raidFree;
    /**
     * 磁盘阵列配置率
     */
    private  Double radeConfigRate;

}
