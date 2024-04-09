package com.jcca.web.collect.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 端口占用
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("COLLECT_PORT")
public class CollectPort  extends Model<CollectPort> {
    public static final String TCP = "TCP";
    public static final String UDP = "UDP";
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ID_WORKER_STR)
    private String id;
    /**
     * 资产ID
     */
    @TableField("ASSET_ID")
    private String assetId;
    /**
     * TCP/UDP
     */
    @TableField("TYPE")
    private String type;
    /**
     * 端口号
     */
    @TableField("PORT_NUM")
    private String portNum;
    /**
     * 端口占用率
     */
    @TableField("PORT_USED_RATE")
    private Double portUsedRate;
    /**
     * 占用总数
     */
    @TableField("USED_COUNT")
    private Integer usedCount;
    /**
     * 采集时间
     */
    @TableField("COLLECT_TIME")
    private Date collectTime;
    /**
     * 创建时间
     */
    @TableField(value = "CREATE_TIME", fill = FieldFill.INSERT)
    private Date createTime;
}
