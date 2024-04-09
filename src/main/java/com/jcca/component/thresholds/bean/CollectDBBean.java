package com.jcca.component.thresholds.bean;

import com.jcca.web.collect.entity.CollectDBfile;
import com.jcca.web.collect.entity.CollectTablespace;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 数据库采集
 *
 * @author Lvyp
 */
@Data
public class CollectDBBean {

    /**
     * 资产主键
     */
    @NotEmpty(message = "资产主键空")
    private String assetId;
    /**
     * 采集时间
     */
    @NotEmpty(message = "采集时间空")
    private String collectTime;
    /**
     * 数据库类型
     * CollectDBTypeEnum
     * ORACLE\MYSQL
     */
    @NotNull(message = "数据库类型空")
    private Byte dbType;
    /**
     * 数据库版本
     */
    @NotEmpty(message = "数据库版本空")
    private String dbVersion;
    /**
     * 启动时间 单位毫秒
     */
    @NotNull(message = "数据库启动时长空")
    private Long sysUpTime;
    /**
     * 缓存命中率
     */
    @NotEmpty(message = "数据库缓存命中率空")
    private String cacheLibrary;
    /**
     * 数据库状态
     * StatusEnum
     * 1正常0不正常
     */
    @NotNull(message = "数据库状态空")
    private String status;
    /**
     * 数据库的运存总量 单位b
     */
    @NotNull(message = "数据库运行内存总量空")
    private Long dbMemTotal;
    /**
     * 硬盘总量 单位b
     */
//    @NotNull(message = "硬盘总量空")
    private Long dbDiskTotal;
    /**
     * 缓存大小
     */
    @NotNull(message = "缓存大小空")
    private Long dbCache;
    /**
     * 繁忙率
     */
    @NotEmpty(message = "繁忙率空")
    private String dbBusynessRate;
    /**
     * 数据库的session数
     */
    @NotNull(message = "session数量空")
    private Long dbSessionSize;
    /**
     * 数据库使用率
     */
    @NotEmpty(message = "数据库使用率空")
    private String dbSessionUsedRate;
    /**
     * 缓存池大小
     */
    @NotNull(message = "缓存池大小空")
    private Long dbCachePoolSize;
    /**
     * 缓存池命中率
     */
    @NotEmpty(message = "缓存池命中率空")
    private String dbCachePoolhit;
    /**
     * 可用锁数量
     */
    @NotNull(message = "可用锁数量空")
    private Long canUseLockSize;
    /**
     * 锁使用率
     */
    @NotEmpty(message = "锁使用率空")
    private String dbLockUsedRate;
    /**
     * 数据库锁的等待率
     */
    @NotEmpty(message = "锁等待率空")
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
    private List<CollectTablespace> tablespace;

}
