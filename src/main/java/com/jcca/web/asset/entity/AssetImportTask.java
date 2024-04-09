package com.jcca.web.asset.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * @ Author：sophia
 * @ Date：Created in 16:31 2021/7/14
 * @ Description:
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ASSET_TASK")
public class AssetImportTask extends Model<AssetImportTask> implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(value = "id", type = IdType.ID_WORKER_STR)
    private String id;

    /**
     * 任务属性2
     */
    @TableField("TITLE")
    private String title;

    /**
     * 任务属性1
     */
    @TableField("HEADER")
    private String header;

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
     * 结束时间
     */
    @TableField(value = "END_TIME")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date endTime;
}
