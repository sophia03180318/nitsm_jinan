package com.jcca.web2.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 数据库采集
 * 库的基本信息
 *
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("COLLECT_DATABASES_INFO")
public class CollectDatabasesInfo  extends Model<CollectDatabasesInfo> {


    @TableId(value = "id", type = IdType.ID_WORKER_STR)
    private String id;
    /**
     * 数据库名称
     */
    @TableField("NAME")
    private String name;
    /**
     * 编码方式
     */
    @TableField("ENCODING")
    private String encoding;
    /**
     * 表空间名称
     */
    @TableField("TABLE_SPEC_NAME")
    private String tableSpecName;
    /**
     * 是否允许客户端连接
     */
    @TableField("PERMIT_AGENT_LINK")
    private Boolean permitAgentLink;
    /**
     * 是否是模板库
     */
    @TableField("IS_TEMPLATE")
    private Boolean isTemplate;
    /**
     * 排序规则
     */
    @TableField("DATA_COLLATE")
    private String dataCollate;
    /**
     * 数据库类型
     */
    @TableField("TYPE")
    private String type;
    /**
     * 数据库ID
     * M_DB表的ID
     */
    @TableField("DB_ID")
    private String dbId;

    /**
     * 创建时间
     */
    @TableField(value = "CREATE_TIME", fill = FieldFill.INSERT)
    private Date createTime;


}
