package com.jcca.dataProcessing.Entity;

import lombok.Data;

/**
 * 数据库信息
 */
@Data
public class DatabasesBeanEntity {

    /**
     * 数据库名称
     */
    private String name;
    /**
     * 编码方式
     */
    private String encoding;
    /**
     * 表空间名称
     */
    private String tableSpecName;
    /**
     * 是否允许客户端连接
     */
    private Boolean permitAgentLink;
    /**
     * 是否是模板库
     */
    private Boolean isTemplate;
    /**
     * 排序规则
     */
    private String dataCollate;
    /**
     * 数据库类型
     */
    private String type;
}
