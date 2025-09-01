package com.jcca.dataProcessing.Entity;



import lombok.Data;

import java.io.Serializable;

/**
 * 采集管理口的CPU信息
 */
@Data
public class CollectBhmTempEntity extends CommonEntity implements Serializable {

    /**
     * 名称
     */
    private String name;
    /**
     * 代表第几个
     */
    private String memberId;
    /**
     * 物理场景定义
     * "Room"，表示基于机房环境基准监控（非密闭空间）
     */
    private String physicalContext;
    /**
     * 温度 可空
     * 摄氏度
     */
    private Double readingCelsius;
    /**
     * 硬件层面的传感器编号
     * 用于定位物理传感器在主板上的焊接 / 部署位置
     * （如 SensorNumber=1 对应进风口传感器）
     */
    private String sensorNumber;
    /**
     * 状态
     */
    private ReadFishStatusEntity status;
    /**
     * 轻度阈值
     */
    private Double upperThresholdNonCritical;
    /**
     * 中度阈值
     */
    private Double upperThresholdCritical;
    /**
     * 重度阈值
     */
    private Double upperThresholdFatal;

}
