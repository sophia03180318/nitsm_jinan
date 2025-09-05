package com.jcca.dataProcessing.Entity;

import lombok.Data;

/**
 * 数据库进程锁
 */
@Data
public class DbProcessLockEntity {

    /**
     * 等待进程ID
     */
    private String blockedPid;
    /**
     * 等待进程ID对应的进程ID
     */
    private String blockedUser;
    /**
     * 正在占用的进程ID
     */
    private String blockingPid;
    /**
     * 正在占用的进程ID对应的用户
     */
    private String blockingUser;
    /**
     * 等待进行的操作
     */
    private String blockedStatement;
    /**
     * 正在进行的操作
     */
    private String blockingStatement;

}
