package com.jcca.web2.entity;


import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;



@EqualsAndHashCode(callSuper = true)
@Data
@TableName("COLLECT_DB_LOCK_INFO")
public class CollectDbLockInfo extends Model<CollectDbLockInfo> {


    @TableId(value = "ID", type = IdType.ID_WORKER_STR)
    private String id;

    @TableField("COLLECT_DB_ID")
    private String collectDbId;
    @TableField("ASSET_ID")
    private String assetId;
    /**
     * 锁类型
     * relation
     * 表级锁，针对整个表对象的锁定（如 SELECT ... FOR UPDATE 对表加的锁）。
     * extend
     * 表扩展锁，用于控制表空间扩展（如新增数据页时的锁定）。
     * page
     * 页级锁，针对表中特定数据页（8KB 为单位）的锁定。
     * tuple
     * 行级锁，针对表中具体行记录的锁定（最常见的行锁类型）。
     * transactionid
     * 事务 ID 锁，用于处理事务间的依赖关系（如 Serializable 隔离级别下的事务排序）。
     * virtualxid
     * 虚拟事务 ID 锁，临时事务（未分配真实事务 ID）使用的锁。
     * speculative token
     * 推测性插入锁，用于并发插入时避免主键冲突的临时锁定。
     * object
     * 数据库对象锁，针对非表类对象（如索引、视图、函数等）的锁定。
     * userlock
     * 用户自定义锁，通过 pg_advisory_lock 等函数手动获取的应用级锁。
     * advisory
     * advisory 锁（与 userlock 类似，用于应用层自定义同步逻辑）。
     */
    @TableField("LOCK_TYPE")
    private String lockType;
    /**
     * 锁模式
     * ACCESS SHARE（访问共享锁）
     * 最弱的锁模式，通常由 SELECT 语句隐式获取。
     * 允许其他事务读取该对象，但阻止其他事务执行 ALTER TABLE、DROP TABLE 等会修改表结构的操作。
     * ROW SHARE（行共享锁）
     * 由 SELECT ... FOR SHARE 语句获取，针对行级。
     * 允许其他事务读取、更新（非锁定行）、插入数据，但阻止排他性表级锁。
     * ROW EXCLUSIVE（行排他锁）
     * 由 INSERT、UPDATE、DELETE 等语句隐式获取（针对受影响的行）。
     * 允许其他事务读取或锁定其他行，但阻止排他性表级锁和某些表级操作。
     * SHARE UPDATE EXCLUSIVE（共享更新排他锁）
     * 用于表级操作（如 VACUUM、ANALYZE），限制较少但会阻止其他事务获取冲突的表级锁。
     * SHARE（共享锁）
     * 由 SELECT ... FOR UPDATE（表级）或 CREATE INDEX 等语句获取。
     * 允许其他事务读取但阻止任何修改操作（包括行级更新）。
     * SHARE ROW EXCLUSIVE（共享行排他锁）
     * 比 SHARE 更强，阻止其他事务获取共享锁或修改数据。
     * 常用于需要协调表级共享访问和部分修改的场景。
     * EXCLUSIVE（排他锁）
     * 较强的锁模式，阻止其他事务读取（除 SELECT 外）或修改被锁定对象。
     * 由 ALTER TABLE 等修改表结构的操作获取。
     * ACCESS EXCLUSIVE（访问排他锁）
     * 最强的锁模式，完全阻止其他事务对该对象的任何访问（包括 SELECT）。
     * 由 DROP TABLE、TRUNCATE 等破坏性操作获取。
     */
    @TableField("LOCK_MODE")
    private String lockMode;
    /**
     * 总请求数
     */
    @TableField("TOTAL_REQUESTS")
    private Long totalRequests;
    /**
     * 已经获得锁的数量
     */
    @TableField("GRANTED_LOCKS")
    private Long grantedLocks;
    /**
     * 锁等待数
     */
    @TableField("WAITING_LOCKS")
    private Long waitingLocks;
    /**
     * 锁获取率
     */
    @TableField("GET_RATE")
    private String getRate;

    /**
     * 创建时间
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField(value = "CREATE_TIME", fill = FieldFill.INSERT)
    private Date createTime;

}
