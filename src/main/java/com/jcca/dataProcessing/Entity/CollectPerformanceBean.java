package com.jcca.dataProcessing.Entity;

import lombok.Data;

/**
 * @author: hhw
 * @description: Linux系统性能采集结果实体类
 * @date: 2025-08-25
 */
@Data
public class CollectPerformanceBean extends CommonEntity {

    private Long collectTime;
    private String assetId;
    private String assetIp;
    private String thresholdProcessId;

    // CPU相关
    private String cpuIdlePercent;           // CPU空闲时间百分比
    private String cpuSystemPercent;         // CPU系统时间百分比
    private String cpuUserPercent;           // CPU用户时间百分比
    private String cpuWaitPercent;           // CPU等待时间百分比
    private String cpuUsageRate;             // CPU使用率
    private String cpuRunningProcessCount;  // CPU运行队列中进程数

    // 内存相关
    private String memoryUsageRate;          // 内存使用率
    private String memoryPageRequests;         // 内存交换请求数
    private String memorySwapInRate;         // 内存交换页换进率
    private String memorySwapOutRate;        // 内存交换页换出率
    private String memoryWaitQueueCount;    // 内存队列数
    private String systemMemoryUsageRate;    // 系统内存使用率
    private String userMemoryUsageRate;      // 用户内存使用率

    // 磁盘相关
    private String diskBusyPercent;          // 磁盘忙的百分比
    private String diskReadBytesPerSec;        // 每秒磁盘读请求字节数
    private String diskWriteBytesPerSec;       // 每秒磁盘写请求字节数

    // 文件系统
    private String filesystemUsageRate;      // 文件系统使用比率
    private String swapUsagePercent;         // 交换区使用百分比
    private String logicalVolumeFsUsageRate; // 逻辑卷文件系统使用率

    // 进程相关
    private String processCpuTimeUsed;                // 占用CPU时间
    private String processStatus;            // 进程状态
    private String processCommand;           // 进程指令行
    private String processStartTime;           // 进程开始时间
    private String processSize;                // 进程空间大小
    private String specificUserProcessCount; // 特定用户进程数
    private String systemFileOpenCount;     // 系统文件打开句柄数
}