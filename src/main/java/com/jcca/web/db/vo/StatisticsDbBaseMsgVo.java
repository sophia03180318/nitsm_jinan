package com.jcca.web.db.vo;

import com.jcca.web.collect.entity.CollectDBfile;
import com.jcca.web.collect.entity.CollectTablespace;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 汇总数据库的基础信息
 *
 * @author Lvyp
 */
@Data
public class StatisticsDbBaseMsgVo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 数据库名称
     */
    private String name;
    /**
     * 实例名称
     */
    private String dbName;
    /**
     * 启动时间 单位毫秒
     */
    private Long sysUpTime;
    /**
     * 启动时间转换单位后的字符串
     */
    private String sysUpTimeStr;
    /**
     * 数据库的session数
     */
    private Long dbSessionSize;
    /**
     * 缓存大小 自适应单位
     */
    private String dbCacheStr;
    /**
     * 内存大小 自适应单位
     */
    private String dbMemTotalStr;

    /**
     * 可用锁数量
     */
    private Long canUseLockSize;
    /**
     * 缓存池大小 自适应单位
     */
    private String dbCachePoolSizeStr;
    /**
     * 资产IP
     */
    private String assetIp;
    /**
     * 数据库驱动
     */
    private String dbTypeStr;

    private String assetImage;
    /**
     * 数据库版本
     */
    private String dbVersion;
    /**
     * java池大小
     */
    private String javaPoolSize;
    /**
     * 大池大小
     */
    private String largePoolSize;
    /**
     * redoLogBuffer
     */
    private String redoLogBuffer;
    /**
     * 数据库连接数
     */
    private String dbConnection;
    /**
     * 数据库活动连接数
     */
    private String dbActive;


    /**
     * 语言环境
     * */
    private String language;

    private Integer dbProtocol;

    /**
     * 数据文件
     */
    private List<CollectDBfile> dataFiles;
    private List<CollectDBfile> controlFiles;
    private List<CollectDBfile> logFiles;
    /**
     * 表空间信息
     */
    private List<CollectTablespace> tablespaceList;


    /**
     * 共享内存：共享缓冲区大小
     * 2025-08-14
     */
    private String sharedBuffers;
    /**
     * 共享内存：使用率
     * 2025-08-14
     */
    private String sharedBuffersUsedRate;

    /**
     * 是否存在死锁
     * 1存在-1 不存在
     * 2025-08-14
     */
    private Integer blockedLock;
    /**
     * 死锁描述
     * 2025-08-14
     */
    private String blockedLockMsg;

    /**
     * 每秒逻辑块读次数
     * 2025-08-15
     */
    private String logicalReadsPerSecond;

    /**
     * 每秒逻辑块写次
     * 2025-08-15
     */
    private String logicalWrPerSecond;

    /**
     * 每秒物理块读
     * 2025-08-15
     */
    private String physicalBlockReads;

    /**
     * 每秒物理块写
     * 2025-08-15
     */
    private String physicalBlockWr;
}
