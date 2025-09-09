package com.jcca.web.asset.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;


/**
 * @author: hhw
 * @description: CollectHardwarePerformance 主要是用来
 * @date: 2025-09-08  15:08
 * @since: 2.1.9.0
 */
@EqualsAndHashCode(callSuper = true)
@TableName("COLLECT_HARDWARE_PERFORMANCE")
@Data
public class CollectHardwarePerformance extends Model<CollectHardwarePerformance> {

    @TableId(value = "ID", type = IdType.ID_WORKER_STR)
    private String id;

    private Date collectTime;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
    private String assetId;
    /**
     * THRESHOLD_PROCESS表ID
     */
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
    private String filesystemUsageRate;      // 文件系统使用比率  磁盘使用率这里不采集
    private String swapUsagePercent;         // 交换区使用百分比
    private String logicalVolumeFsUsageRate; // 逻辑卷文件系统使用率

    // 进程相关
    private String processCpuTimeUsed;                // 占用CPU时间
    /**
     * 进程状态
     * R	Running	进程正在运行或在运行队列中等待
     * S	Sleeping	进程在可中断的睡眠状态（等待事件完成）
     * D	Uninterruptible sleep	不可中断的睡眠状态（通常等待I/O操作）
     * T	Stopped	进程被信号停止（如 Ctrl+Z）
     * t	Tracing stop	进程被调试器跟踪停止
     * Z	Zombie	僵尸进程（已终止但父进程未回收）
     * X	Dead	进程完全死亡（很少见到）
     * <p>
     * <p>
     * 修饰符	含义	说明
     * <	高优先级	进程拥有高优先级（nice值为负）
     * N	低优先级	进程拥有低优先级（nice值为正）
     * L	有锁定的页面	进程有页面被锁定在内存中
     * s	会话领导	进程是会话的领导进程
     * l	多线程	进程是多线程的
     * +	前台进程组	进程在前台进程组中
     * /	Linux 3.0+	新格式分隔符（较少见）
     */
    private String processStatus;            // 进程状态
    private String processCommand;           // 进程指令行
    private String processStartTime;           // 进程开始时间
    private String processSize;                // 进程空间大小
    private String specificUserProcessCount; // 特定用户进程数
    private String systemFileOpenCount;     // 系统文件打开句柄数
}
