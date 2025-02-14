package com.jcca.web.collect.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 采集表空间
 *
 * @author hanwone
 * @date 2020-08-06 09:55:08
 **/
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("collect_tablespace")
public class CollectTablespace extends Model<CollectTablespace> implements java.io.Serializable {

    private static final long serialVersionUID = 1L;


    /**
     * 数据ID
     */
    @TableId(value = "ID", type = IdType.ID_WORKER_STR)
    private String id;
    /**
     * 表空间名称
     */
    @TableField("NAME")
    private String name;

    /**
     * 状态
     */
    @TableField("STATUS")
    private String status;

    /**
     * 表空间总大小M
     */
    @TableField("TOTAL_SIZE")
    private Long totalSize;
    /**
     * 空闲大小M
     */
    @TableField("FREE_SIZE")
    private Long freeSize;
    /**
     * 使用大小M
     */
    @TableField("USED_SIZE")
    private Long usedSize;
    /**
     * 使用率%
     */
    @TableField("USED_RATE")
    private Double usedRate;

    /**
     * 表空间最大扩展大小 M
     */
    @TableField("MAX_SIZE")
    private Long maxSize;
    /**
     * 采集时间
     */
    @TableField("COLLECT_TIME")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date collectTime;
    /**
     * 创建时间
     */
    @TableField(value = "CREATE_TIME", fill = FieldFill.INSERT)
    private Date createTime;
    /**
     * 资产ID
     */
    @TableField("ASSET_ID")
    private String assetId;
    /**
     * collect_db主键ID
     */
    @TableField("COLLECT_DB_ID")
    private String collectDbId;



}