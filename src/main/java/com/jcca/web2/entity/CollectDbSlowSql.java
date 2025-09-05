package com.jcca.web2.entity;


import com.baomidou.mybatisplus.annotation.*;

import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("COLLECT_SLOW_SQL")
public class CollectDbSlowSql extends Model<CollectDbSlowSql> implements java.io.Serializable {

    @TableId(value = "ID", type = IdType.ID_WORKER_STR)
    private String id;

    @TableField("COLLECT_DB_ID")
    private String collectDbId;
    /**
     * 慢sql
     */
    @TableField("SLOW_SQL")
    private String slowSql;
    /**
     * 关联数据库名称
     */
    @TableField("DATABASES_NAME")
    private String databaseName;
    /**
     * 执行耗时  毫秒
     */
    @TableField("EXE_TIME")
    private Long exeTime;
    /**
     * 执行次数
     */
    @TableField("EXE_COUNT")
    private Long exeCount;
    /**
     * 创建时间
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField(value = "CREATE_TIME", fill = FieldFill.INSERT)
    private Date createTime;

}
