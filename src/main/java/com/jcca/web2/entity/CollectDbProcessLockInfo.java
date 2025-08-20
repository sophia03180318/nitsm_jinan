package com.jcca.web2.entity;


import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * King数据库当前锁信息
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("COLLECT_DA_PROCESS_LOCK_INFO")
public class CollectDbProcessLockInfo extends Model<CollectDbProcessLockInfo> {


    @TableId(value = "id", type = IdType.ID_WORKER_STR)
    private String id;
    /**
     * 等待进程ID
     */
    @TableField("BLOCKED_PID")
    private String blockedPid;
    /**
     * 等待进程ID对应的进程ID
     */
    @TableField("BLOCKED_USER")
    private String blockedUser;
    /**
     * 正在占用的进程ID
     */
    @TableField("BLOCKING_PID")
    private String blockingPid;
    /**
     * 正在占用的进程ID对应的用户
     */
    @TableField("BLOCKING_USER")
    private String blockingUser;
    /**
     * 等待进行的操作
     */
    @TableField("BLOCKED_STATEMENT")
    private String blockedStatement;
    /**
     * 正在进行的操作
     */
    @TableField("BLOCKING_STATEMENT")
    private String blockingStatement;
    /**
     * 创建时间
     */
    @TableField(value = "CREATE_TIME", fill = FieldFill.INSERT)
    private Date createTime;

}
