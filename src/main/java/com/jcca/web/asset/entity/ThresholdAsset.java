package com.jcca.web.asset.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.JdbcType;

import java.util.Date;

/**
 * 阈值管理
 *
 * @author hanwone
 * @date 2020-05-20 10:27:48
 **/
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("THRESHOLD_ASSET")
public class ThresholdAsset extends Model<ThresholdAsset> implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 资产ID
     */
    @TableId(value = "ASSET_ID", type = IdType.ID_WORKER_STR)
    private String assetId;
    /**
     * 资产类型
     */
    @TableField("ASSET_MODE")
    private Integer assetMode;
    /**
     * CPU使用率
     */
    @TableField(value = "CPU", updateStrategy = FieldStrategy.IGNORED, jdbcType = JdbcType.DOUBLE)
    private Double cpu;
    /**
     * 磁盘使用率
     */
    @TableField(value = "DISK", updateStrategy = FieldStrategy.IGNORED, jdbcType = JdbcType.DOUBLE)
    private Double disk;
    /**
     * 内存使用率
     */
    @TableField(value = "MEMORY", updateStrategy = FieldStrategy.IGNORED, jdbcType = JdbcType.DOUBLE)
    private Double memory;
    /**
     * 接收丢包率
     */
    @TableField(value = "PACKET_LOSS_IN", updateStrategy = FieldStrategy.IGNORED, jdbcType = JdbcType.DOUBLE)
    private Double packetLossIn;
    /**
     * 发送丢包率
     */
    @TableField(value = "PACKET_LOSS_OUT", updateStrategy = FieldStrategy.IGNORED, jdbcType = JdbcType.DOUBLE)
    private Double packetLossOut;
    /**
     * 接收误码率
     */
    @TableField(value = "CODE_ERROR_IN", updateStrategy = FieldStrategy.IGNORED, jdbcType = JdbcType.DOUBLE)
    private Double codeErrorIn;
    /**
     * 发送误码率
     */
    @TableField(value = "CODE_ERROR_OUT", updateStrategy = FieldStrategy.IGNORED, jdbcType = JdbcType.DOUBLE)
    private Double codeErrorOut;
    /**
     * 端口流入百分比
     */
    @TableField(value = "PORT_RATE_IN", updateStrategy = FieldStrategy.IGNORED, jdbcType = JdbcType.DOUBLE)
    private Double portRateIn;
    /**
     * 端口流出百分比
     */
    @TableField(value = "PORT_RATE_OUT", updateStrategy = FieldStrategy.IGNORED, jdbcType = JdbcType.DOUBLE)
    private Double portRateOut;
    /**
     * 时间偏差
     */
    @TableField(value = "TIME_DEVIATION", updateStrategy = FieldStrategy.IGNORED, jdbcType = JdbcType.INTEGER)
    private Integer timeDeviation;
    /**
     * 运行时长 天
     */
    @TableField(value = "RUNNINGTIME_DEVIATION", updateStrategy = FieldStrategy.IGNORED, jdbcType = JdbcType.INTEGER)
    private Integer runningTimeDeviation;
    /**
     * 表空间
     */
    @TableField(value = "TABLE_SPACE", updateStrategy = FieldStrategy.IGNORED, jdbcType = JdbcType.INTEGER)
    private Integer tablespace;
    /**
     * CPU负载
     */
    @TableField(value = "CPU_LOAD")
    private Double cpuLoad;
    /**
     * 阈值类型标记，1默认阈值，2手动单个阈值
     * ThresholdAutoFlagEnum
     */
    @TableField("AUTO_FLAG")
    private Byte autoFlag;
    /**
     * 创建时间
     */
    @TableField(value = "CREATE_TIME", fill = FieldFill.INSERT)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
    /**
     * 创建者
     */
    @TableField(value = "CREATOR", fill = FieldFill.INSERT)
    private String creator;
    /**
     * 修改时间
     */
    @TableField(value = "MODIFY_TIME", fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date modifyTime;
    /**
     * 修改者
     */
    @TableField(value = "MODIFIER", fill = FieldFill.INSERT_UPDATE)
    private String modifier;

}