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

    @TableField("COLLECT_DB_ID")
    private String collectDbId;
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
     * 1允许 -1不允许
     */
    @TableField("PERMIT_AGENT_LINK_STATUS")
    private Integer permitAgentLinkStatus;
    @TableField(exist = false)
    private Boolean permitAgentLink;

    /**
     * 是否是模板库
     * 1是 -1不是
     */
    @TableField("IS_TEMPLATE_FLAG")
    private Integer isTemplateFlag;
    @TableField(exist = false)
    private Boolean isTemplate;

    /**
     * 排序规则
     */
    @TableField("DATA_COLLATE")
    private String dataCollate;
    /**
     * 创建时间
     */
    @TableField(value = "CREATE_TIME", fill = FieldFill.INSERT)
    private Date createTime;


}
