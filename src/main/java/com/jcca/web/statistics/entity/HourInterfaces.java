package com.jcca.web.statistics.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 端口一小时统计
 *
 * @author Lvyp
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("HOUR_INTERFACES")
public class HourInterfaces extends Model<HourInterfaces> {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ID_WORKER_STR)
    private String id;
    /**
     * 资产主键
     */
    @TableField("ASSET_ID")
    private String assetId;
    /**
     * 端口流入 b
     */
    @TableField("PORT_IN")
    private Long portIn;
    /**
     * 端口流出 b
     */
    @TableField("PORT_OUT")
    private Long portOut;
    /**
     * 端口流入丢包数
     */
    @TableField("DISCARD_PACKETS_IN")
    private Long discardPacketsIn;
    /**
     * 端口流出丢包数
     */
    @TableField("DISCARD_PACKETS_OUT")
    private Long discardPacketsOut;
    /**
     * 非单播流入量 单位：b
     */
    @TableField("NO_UNICAST_PACKETS_IN")
    private Long noUnicastPacketsIn;
    /**
     * 非单播流出量 单位：b
     */
    @TableField("NO_UNICAST_PACKETS_OUT")
    private Long noUnicastPacketsOut;
    /**
     * 单播流入量 单位：b
     */
    @TableField("UNICAST_PACKETS_IN")
    private Long unicastPacketsIn;
    /**
     * 单播流出量 单位：b
     */
    @TableField("UNICAST_PACKETS_OUT")
    private Long unicastPacketsOut;
    /**
     * 误码流入量 单位：b
     */
    @TableField("ERROR_CODE_IN")
    private Long errorCodeIn;
    /**
     * 误码流出量 单位：b
     */
    @TableField("ERROR_CODE_OUT")
    private Long errorCodeOut;
    /**
     * 端口速率
     * b/s
     */
    @TableField("PORT_SPEED")
    private Long portSpeed;
    /**
     * 端口接收率
     * b/s
     */
    @TableField("PORT_IN_SPEED")
    private Long portInSpeed;
    /**
     * 端口发送率 数字格式
     * s/b
     */
    @TableField("PORT_OUT_SPEED")
    private Long portOutSpeed;
    /**
     * 端口发送丢包率 百分比-数字格式
     */
    @TableField("LOSE_PACKETS_RATE_OUT")
    private Double losePacketsRateOut;
    /**
     * 端口接收丢包率 百分比-数字格式
     */
    @TableField("LOSE_PACKETS_RATE_IN")
    private Double losePacketsRateIn;
    /**
     * 端口发送错误率 百分比-数字格式
     */
    @TableField("ERRO_CODE_RATE_OUT")
    private Double erroCodeRateOut;
    /**
     * 端口接收错误率 百分比-数字格式
     */
    @TableField("ERRO_CODE_RATE_IN")
    private Double erroCodeRateIn;
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
}
