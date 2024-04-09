package com.jcca.admin.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * @ Author：sophia
 * @ Date：Created in 9:52 2021/8/12
 * @ Description:
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("cabinet_task")
public class CabinetTask extends Model<CabinetTask> implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * id
     */
    @TableId(value = "id", type = IdType.ID_WORKER_STR)
    private String id;

    /**
     * 任务状态
     * 1:running; 0:stop;
     */
    @TableField("STATUS")
    private Integer status;

    /**
     * 成功条数
     */
    @TableField("SUCCESS")
    private Integer success;

    /**
     * 失败条数
     */
    @TableField("FAIL")
    private Integer fail;
    /**
     * 总条数
     */
    @TableField("COUNT")
    private Integer count;
    /**
     * 任务日志
     */
    @TableField("LOG")
    private String log;

    /**
     * 创建时间
     */
    @TableField(value = "CREATE_TIME", fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 前台进度条百分比
     */
    @TableField(exist = false)
    private String bar;

}
