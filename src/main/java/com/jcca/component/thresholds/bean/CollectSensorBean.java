package com.jcca.component.thresholds.bean;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;

/**
 * 环境传感采集数据处理
 *
 * @author Lvyp
 */
@Data
public class CollectSensorBean implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 采集时间
     * 时间戳
     */
    @NotEmpty(message = "采集时间空")
    private String collectTime;
    /**
     * 资产ID
     */
    @NotEmpty(message = "资产ID空")
    private String assetId;
    /**
     * 序列号
     */
    @NotEmpty(message = "序列号空")
    private String serialNumberName;
    /**
     * 传感类型
     */
    @NotEmpty(message = "传感类型空")
    private String sensorType;
    /**
     * 采集到的值
     */
    @NotEmpty(message = "采集到的值空")
    private String value;
    /**
     * 状态
     */
    @NotEmpty(message = "状态空")
    private String status;
    /**
     * 描述信息
     */
    @NotEmpty(message = "描述信息空")
    private String descStr;

}
