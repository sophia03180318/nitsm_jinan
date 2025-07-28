package com.jcca.admin.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * @ Author：sophia
 * @ Date：Created in 9:52 2021/8/20
 * @ Description:
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("PERFORMANCE_TARGET")
public class PerformanceTarget extends Model<PerformanceTarget> implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(value = "id", type = IdType.ID_WORKER_STR)
    private String id;

    /**
     * 设备类型
     * 183：主机，42：路由器，201：交换机，263：oracle数据库
     */
    @TableField("ASSET_MODE")
    private Integer assetMode;

    /**
     * 指标标记
     */
    @TableField("SPEC_ID")
    private Integer specId;

    /**
     * 指标是否可用
     * 1:可用,0:不可用
     */
    @TableField("IS_AVAILABLE")
    private Integer isAvailable;

    /**
     *
     */
    @TableField("TARGET_HANDLE")
    private String targetHandle;

    /**
     * 指标描述
     */
    @TableField("TARGET_DESCRIPTION")
    private String targetDescription;

    /**
     * 定时表达式
     */
    @TableField("CRON_EXPRESS")
    private String cronExpress;

    /**
     * 指标命令
     */
    @TableField(value = "COMMAND")
    private String command;

    /**
     * 指标名称
     */
    @TableField(value = "COMMAND_NAME")
    private String commandName;

    @TableField(value = "EXE_COMMAND")
    private String exeCommand;

    /**
     *
     */
    @TableField(value = "SLEEP_TIME")
    private Integer sleepTime;

    /**
     * 是否启动Ping隧道
     * 1:启用
     */
    @TableField(value = "PING_TUNNEL")
    private Integer pingTunnel;

    /**
     * 创建时间
     */
    @TableField(value = "CREATE_TIME", fill = FieldFill.INSERT)
    private Date createTime;


}
