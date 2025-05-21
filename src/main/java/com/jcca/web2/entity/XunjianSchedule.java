package com.jcca.web2.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;
import java.util.List;

/**
 * @author: hhw
 * @description: XunjianSchedule主要是用来保存智能巡检定时任务
 * @date: 2025-02-25  15:51
 * @since: 2.0.11.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("XUNJIAN_SCHEDULE")
public class XunjianSchedule extends Model<XunjianSchedule> {

    private static final long serialVersionUID = 2139925297965371168L;

    @TableId
    private String id;
    /**
     * 任务表达式
     */
    private String cron;
    /**
     * 任务编号
     */
    private String jobId;
    /**
     * 周期时间
     */
    private String cronTime;
    private String jobName;
    private String operator;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date lastTime;
    /**
     * 任务状态，1停止，2执行中
     */
    private Integer jobState;
    /**
     * 任务类型，1手动巡检，2周期巡检
     */
    private Integer autoFlag;
    /**
     * 执行策略，1手动执行，2立即执行
     */
    private Integer startNow;
    private String remark;

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
    @TableField(value = "MODIFY_TIME", fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date modifyTime;
    /**
     * 修改者
     */
    @TableField(value = "MODIFIER", fill = FieldFill.INSERT_UPDATE)
    private String modifier;

    @TableField(exist = false)
    private List<String> cronList;
    @TableField(exist = false)
    private String inspectRecordId;

}
