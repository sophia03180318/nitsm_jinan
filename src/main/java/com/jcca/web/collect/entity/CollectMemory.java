package com.jcca.web.collect.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 处理运行内存
 *
 * @author Lvyp
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("COLLECT_MEMORY")
public class CollectMemory extends Model<CollectMemory> {

    private static final long serialVersionUID = 1L;

    @TableId(value = "ID", type = IdType.ID_WORKER_STR)
    private String id;
    /**
     * 采集时间
     */
    @TableField("COLLECT_TIME")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date collectTime;
    /**
     * 采集编号
     * 同一台设备同一次采集编号相同
     */
    @TableField("COLLECT_CODE")
    private String collectCode;
    /**
     * 资产ID
     */
    @TableField("ASSET_ID")
    private String assetId;
    /**
     * 总量不包括置换内存
     * b
     */
    @TableField("MEM_TOTAL")
    private Long memTotal;
    /**
     * 已使用
     * b
     */
    @TableField("MEM_USED")
    private Long memUsed;
    /**
     * 已用内存使用率
     */
    @TableField("MEM_USED_RATE")
    private Double memUsedRate;
    /**
     * 置换内存总量
     * b
     */
    @TableField("SWAP_TOTAL")
    private Long swapTotal;
    /**
     * 置换内存总量
     * b
     */
    @TableField("SWAP_USED")
    private Long swapUsed;
    /**
     * 置换内存使用率
     */
    @TableField("SWAP_USED_RATE")
    private Double swapUsedRate;
    /**
     * 创建时间
     */
    @TableField(value = "CREATE_TIME", fill = FieldFill.INSERT)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    /**
     * 分组标识
     */
    @TableField(exist = false)
    private String groupFlg;
}
