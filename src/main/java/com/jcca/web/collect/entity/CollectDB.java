package com.jcca.web.collect.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;
import java.util.List;

/**
 * 数据库采集信息
 *
 * @author Lvyp
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("COLLECT_DB")
public class CollectDB extends Model<CollectDB> {

    private static final long serialVersionUID = 1L;

    @TableId(value = "ID", type = IdType.ID_WORKER_STR)
    private String id;
    /**
     * 采集时间
     */
    @TableField("COLLECT_TIME")
    private Date collectTime;
    /**
     * 采集编号 同一台设备同一次采集编号相同
     */
    @TableField("COLLECT_CODE")
    private String collectCode;
    /**
     * 资产ID
     */
    @TableField("ASSET_ID")
    private String assetId;
    /**
     * 数据库类型 ORACLE\MYSQL
     * DBTypeEnum
     */
    @TableField("DB_TYPE")
    private Byte dbType;
    /**
     * 数据库版本
     */
    @TableField("DB_VERSION")
    private String dbVersion;
    /**
     * 运行时长 单位毫秒
     */
    @TableField("SYS_UP_TIME")
    private Long sysUpTime;
    /**
     * 缓存命中率
     */
    @TableField("CACHE_HIT_RATE")
    private Double cacheHitRate;
    /**
     * 数据库状态
     * 1正常0不正常
     */
    @TableField("STATUS")
    private String status;
    /**
     * 数据库的运存大小 单位b
     */
    @TableField("DB_MEM_TOTAL")
    private Long dbMemTotal;
    /**
     * 硬盘大小 单位b
     */
    @TableField("DB_DISK_TOTAL")
    private Long dbDiskTotal;
    /**
     * 缓存大小
     */
    @TableField("DB_CACHE")
    private Long dbCache;
    /**
     * 繁忙率
     */
    @TableField("DB_BUSYNESS_RATE")
    private Double dbBusynessRate;
    /**
     * 数据库的session数
     */
    @TableField("DB_SESSION_SIZE")
    private Long dbSessionSize;
    /***
     * 数据库缓存使用率
     */
    @TableField("DB_SESSION_USED_RATE")
    private Double dbSessionUsedRate;
    /**
     * 缓存池大小
     */
    @TableField("DB_CACHE_POOL_SIZE")
    private Long dbCachePoolSize;
    /**
     * 共享池命中率
     */
    @TableField("DB_CACHE_POOL_HIT")
    private Double dbCachePoolhit;
    /**
     * 可用锁数量
     */
    @TableField("CAN_USE_LOCK_SIZE")
    private Long canUseLockSize;
    /**
     * 锁使用率
     */
    @TableField("DB_LOCK_USED_RATE")
    private Double dbLockUsedRate;
    /**
     * 数据库锁的等待率
     */
    @TableField("DB_LOCK_WAIT_RATE")
    private Double dbLockWaitRate;
    /**
     * 数据库锁的等待时间 单位毫秒
     */
    @TableField("DB_LOCK_WAIT_TIME")
    private Long dbLockWaitTime;
    /**
     * 创建时间
     */
    @TableField(value = "CREATE_TIME", fill = FieldFill.INSERT)
    private Date createTime;
    /**
     * 语言环境
     */
    @TableField("LANGUAGE")
    private String language;

    /**
     * 日志地址
     */
    @TableField("ALERT_PATH")
    private String alertPath;
    /**
     * java池大小
     */
    @TableField(exist = false)
    private String javaPoolSize;
    /**
     * 大池大小
     */
    @TableField(exist = false)
    private String largePoolSize;
    /**
     * redoLogBuffer
     */
    @TableField(exist = false)
    private String redoLogBuffer;
    /**
     * 缓冲库命中率
     */
    @TableField(exist = false)
    private String cacheLibrary;
    /**
     * 数据库连接数
     */
    @TableField(exist = false)
    private String dbConnection;
    /**
     * 数据库活动连接数
     */
    @TableField(exist = false)
    private String dbActive;

    /**
     * 数据文件
     */
    @TableField(exist = false)
    private List<CollectDBfile> dbFiles;

    /**
     * 表空间
     */
    @TableField(exist = false)
    private List<CollectTablespace> tablespace;
}
