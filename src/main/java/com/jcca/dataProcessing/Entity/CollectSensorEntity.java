package com.jcca.dataProcessing.Entity;

import lombok.Data;

import java.io.Serializable;

/**
 * 环境传感采集数据处理
 *
 * @author Lvyp
 */
@Data
public class CollectSensorEntity extends CommonEntity implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * 名称
     */
    private String name;
    /**
     * 值
     */
    private String value;
    /**
     * 状态
     */
    private String status;
    /**
     * 信息
     */
    private String msg;

    /**
     * 核数
     */
    private String cores;

    /**
     * 电压(毫伏)
     */
    private String voltage;

    /**
     * 厂商
     */
    private String manu;

    private String sensorType;

    private String serialNumberName;


}
