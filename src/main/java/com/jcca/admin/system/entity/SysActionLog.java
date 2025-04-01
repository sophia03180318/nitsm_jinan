package com.jcca.admin.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 操作日志表
 *
 * @author hanwone
 * @date 2020-04-06 12:11:47
 **/
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("sys_action_log")
public class SysActionLog extends Model<SysActionLog> implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 日志ID
     */
    @TableId(value = "id", type = IdType.ID_WORKER_STR)
    private String id;
    /**
     * 日志模块
     */
    @TableField("LOG_MODEL")
    private String logModel;
    /**
     * 操作动作
     */
    @TableField("LOG_NAME")
    private String logName;
    /**
     * 日志类型 LogTypeConstant
     */
    @TableField("LOG_TYPE")
    private Byte logType;
    /**
     * 产生日志用户IP
     */
    @TableField("OPER_USER_IP")
    private String operUserIp;
    /**
     * 产生日志的类
     */
    @TableField("LOG_CLASS")
    private String logClass;
    /**
     * 产生日志方法
     */
    @TableField("LOG_METHOD")
    private String logMethod;

    /**
     * 作用对象
     */
    @TableField("RECORD_ID")
    private String recordId;
    /**
     * 日志信息
     */
    @TableField("LOG_MSG")
    private String logMsg;
    /**
     * 创建时间
     */
    @TableField(value = "CREATE_TIME", fill = FieldFill.INSERT)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
    /**
     * 创建者
     */
    @TableField(value = "CREATOR", fill = FieldFill.INSERT)
    private String creator;
    /**
     * 修改时间
     */
    @TableField(value = "MODIFY_TIME", fill = FieldFill.UPDATE)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date modifyTime;
    /**
     * 修改者
     */
    @TableField(value = "MODIFIER", fill = FieldFill.UPDATE)
    private String modifier;


    @TableField(exist = false)
    private Boolean flag;
    @TableField(exist = false)
    private Date startTime;
    @TableField(exist = false)
    private Date endTime;
}