package com.jcca.dataProcessing.Entity;

import com.jcca.web.collect.entity.CollectDBfile;
import com.jcca.web2.entity.CollectDbSlowSql;
import lombok.Data;

import java.util.List;

/**
 * 数据库采集
 *
 * @author Lvyp
 */
@Data
public class CollectDBEntity extends CommonEntity {


    /**
     * 数据库类型
     * CollectDBTypeEnum
     * ORACLE\MYSQL
     */
    private Byte dbType;
    /**
     * 数据库版本
     */
    private String dbVersion;
    /**
     * 启动时间 单位毫秒
     */
    private Long sysUpTime;
    /**
     * 缓存命中率
     */
    private String cacheLibrary;
    /**
     * 数据库状态
     * StatusEnum
     * 1正常0不正常
     */
    private String status;
    /**
     * 数据库的运存总量 单位b
     */
    private Long dbMemTotal;
    /**
     * 硬盘总量 单位b
     */
    private Long dbDiskTotal;
    /**
     * 缓存大小
     */
    private Long dbCache;
    /**
     * 繁忙率
     */
    private String dbBusynessRate;
    /**
     * 数据库的session数
     */
    private Long dbSessionSize;
    /**
     * 数据库使用率
     */
    private String dbSessionUsedRate;
    /**
     * 缓存池大小
     */
    private Long dbCachePoolSize;
    /**
     * 缓存池命中率
     */
    private String dbCachePoolhit;
    /**
     * 可用锁数量
     */
    private Long canUseLockSize;
    /**
     * 锁使用率
     */
    private String dbLockUsedRate;
    /**
     * 数据库锁的等待率
     */
    private String dbLockWaitRate;
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


    /**
     * 告警文件地址
     * */
    private String alertPath;

    /**
     * 数据文件
     */
    private List<CollectDBfile> dbFiles;

    /**
     * 表空间
     */
    private List<CollectTablespaceEntity> tablespace;

    /**
     * 数据库信息列表、
     * 2025-08-14
     */
    private List<DatabasesBeanEntity> databasesInfoList;
    /**
     * 数据库日志归档配置信息
     * 2025-08-14
     */
    private List<DbLogSettingEntity> logSettingList;

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
     * 当前锁列表
     * 2025-08-14
     */
    private List<DbProcessLockEntity> processLockList;
    /**
     * 内部锁列表信息
     */
    private List<DbLockInfoEntity> lockInfoList;
    /**
     * 是否存在死锁
     * 2025-08-14
     */
    private Boolean blockedLock;
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
    /**
     * 慢sql
     * 2025-08-15
     */
    private List<CollectDbSlowSql> slowSqlList;

}
