package com.jcca.web.statistics.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 设备最高温度一小时数据汇总
 *
 * @author Lvyp
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("HOUR_TEMP")
public class HourTemp extends Model<HourTemp> {

    private static final long serialVersionUID = 1L;

    @TableId(value = "ID", type = IdType.ID_WORKER_STR)
    private String id;
    /**
     * 资产ID
     */
    @TableField("ASSET_ID")
    private String assetId;
    /**
     * 温度
     */
    @TableField("TEMP_VALUE")
    private String tempValue;
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
