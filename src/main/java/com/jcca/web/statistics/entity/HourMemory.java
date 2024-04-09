package com.jcca.web.statistics.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 内存一小时统计
 *
 * @author Lvyp
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("HOUR_MEMORY")
public class HourMemory extends Model<HourMemory> {

    private static final long serialVersionUID = 1L;

    @TableId(value = "ID", type = IdType.ID_WORKER_STR)
    private String id;
    /**
     * 资产ID
     */
    @TableField("ASSET_ID")
    private String assetId;
    /**
     * 总量不包括置换内存 b
     */
    @TableField("MEM_TOTAL")
    private Long memTotal;
    /**
     * 已使用 b
     */
    @TableField("MEM_USED")
    private Long memUsed;
    /**
     * 已用内存使用率
     */
    @TableField("MEM_USED_RATE")
    private Double memUsedRate;
    /**
     * 置换内存总量 b
     */
    @TableField("SWAP_TOTAL")
    private Long swapTotal;
    /**
     * 置换内存使用总量 b
     */
    @TableField("SWAP_USED")
    private Long swapUsed;
    /**
     * 置换内存使用率
     */
    @TableField("SWAP_USED_RATE")
    private Double swapUsedRate;
    /**
     * 统计的年
     */
    @TableField("STATISTICS_YEAR")
    private Integer statisticsYear;
    /**
     * 统计的月
     */
    @TableField("STATISTICS_MONTH")
    private Integer statisticsMonth;
    /**
     * 统计的日
     */
    @TableField("STATISTICS_DAY")
    private Integer statisticsDay;
    /**
     * 统计的小时 x时
     */
    @TableField("STATISTICS_HOUR")
    private Integer statisticsHour;
    /**
     * 统计段的截止时间
     */
    @TableField("END_TIME")
    private Date endTime;
    /**
     * 创建时间
     */
    @TableField(value = "CREATE_TIME", fill = FieldFill.INSERT)
    private Date createTime;
    @TableField(exist = false)
    private String assetName;
}
