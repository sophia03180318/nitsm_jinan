package com.jcca.web2.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.jcca.dataProcessing.Entity.ReadFishStatusEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 温度信息
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("COLLECT_BHM_TEMP_INFO")
public class CollectBhmTempInfo extends Model<CollectBhmTempInfo> {

    public static final String NORMAL_HEALTH = "OK";

    @TableId(value = "ID", type = IdType.ID_WORKER_STR)
    private String id;

    @TableField("ASSET_ID")
    private String assetId;
    /**
     * 名称
     */
    @TableField("NAME")
    private String name;
    /**
     * 代表第几个
     */
    @TableField("MEMBER_ID")
    private String memberId;
    /**
     * 物理场景定义
     * "Room"，表示基于机房环境基准监控（非密闭空间）
     */
    @TableField("PHYSICAL_CONTEXT")
    private String physicalContext;
    /**
     * 温度 可空
     * 摄氏度
     */
    @TableField("READING_CELSIUS")
    private Double readingCelsius;
    /**
     * 硬件层面的传感器编号
     * 用于定位物理传感器在主板上的焊接 / 部署位置
     * （如 SensorNumber=1 对应进风口传感器）
     */
    @TableField("SENSOR_NUMBER")
    private String sensorNumber;
    /**
     * 轻度阈值
     */
    @TableField("UPPER_THRESHOLD_NON_CRITICAL")
    private Double upperThresholdNonCritical;
    /**
     * 中度阈值
     */
    @TableField("UPPER_THRESHOLD_CRITICAL")
    private Double upperThresholdCritical;
    /**
     * 重度阈值
     */
    @TableField("UPPER_THRESHOLD_FATAL")
    private Double upperThresholdFatal;

    /**
     * 健康状态
     */
    @TableField("HEALTH")
    private String health;
    /**
     * 是否启用
     * Enabled 启用
     */
    @TableField("STATE")
    private String state;

    /**
     * 采集批次码
     */
    @TableField("COLLECT_CODE")
    private String collectCode;
    /**
     * 创建时间
     */
    @TableField(value = "CREATE_TIME", fill = FieldFill.INSERT)
    private Date createTime;

}
