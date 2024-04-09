package com.jcca.web.collect.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * @ Author：sophia
 * @ Date：Created in 15:51 2023/2/1
 * @ Description: sophia
 * <p>
 * 采集 oracle数据文件状态
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("COLLECT_DB_FILE")
public class CollectDBfile extends Model<CollectDBfile> {

    private static final long serialVersionUID = 1L;
    /**
     * 数据ID
     */
    @TableId(value = "ID", type = IdType.ID_WORKER_STR)
    private String id;

    @TableField("ASSET_ID")
    private String assetId;
    /**
     * CollectDB主键ID
     */
    @TableField("COLLECT_DB_ID")
    private String collectDbId;

    /**
     * 1=数据文件 3=控制文件 3=日志文件
     */
    @TableField("CATEGORY")
    private Integer category;

    /**
     * 文件名
     */
    @TableField("NAME")
    private String name;
    /**
     * 状态
     */
    @TableField("STATUS")
    private String status;
    /**
     * 类型
     */
    @TableField("TYPE")
    private String type;

    /**
     * 是否健康
     */
    @TableField("HEALTH")
    private String health;

    /**
     * 采集时间
     */
    @TableField("COLLECT_TIME")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date collectTime;


}
