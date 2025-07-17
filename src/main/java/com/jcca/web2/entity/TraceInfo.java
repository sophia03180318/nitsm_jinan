package com.jcca.web2.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 追踪管理
 *
 * @author sophia
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("TRACE_INFO")
public class TraceInfo extends Model<TraceInfo> {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ID_WORKER_STR)
    private String id;
    /**
     * ALARM_ID
     */
    @TableField("ALARM_ID")
    private String alarmId;

    /**
     * 处理内容
     */
    @TableField("CONTENT")
    private String content;

    /**
     * 处理时间
     */
    @TableField(value = "CREATE_TIME", fill = FieldFill.INSERT)
    private Date createTime;
    /**
     * 处理人
     */
    @TableField(value = "CREATOR", fill = FieldFill.INSERT)
    private String creator;

    /**
     * 处理状态 1=追踪 2=结束
     * */
    @TableField(exist = false)
    private Integer status;

}
