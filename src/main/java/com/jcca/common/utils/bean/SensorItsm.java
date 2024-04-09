package com.jcca.common.utils.bean;

import lombok.Data;

/**
 * 传感
 *
 * @author Lvyp
 */
@Data
public class SensorItsm {
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
}
