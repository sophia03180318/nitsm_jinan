package com.jcca.web.statistics.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * CPU一小时数据汇总
 *
 * @author Lvyp
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("HOUR_CPU")
public class HourCpu extends Model<HourCpu> {

    private static final long serialVersionUID = 1L;

    @TableId(value = "ID", type = IdType.ID_WORKER_STR)
    private String id;
    /**
     * 资产ID
     */
    @TableField("ASSET_ID")
    private String assetId;
    /**
     * cpu使用率
     */
    @TableField("CPU_USED_RATE")
    private Double cpuUsedRate;
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
     * 统计的小时
     * x时
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

    /**
     * 分组标记
     */
    @TableField(exist = false)
    private Integer groupFlg;

    @TableField(exist = false)
    private String assetName;
}
