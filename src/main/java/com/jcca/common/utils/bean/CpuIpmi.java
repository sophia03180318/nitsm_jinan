package com.jcca.common.utils.bean;

import lombok.Data;

/**
 * IBM管理口数据
 *
 * @author sophia
 */
@Data
public class CpuIpmi {

    /**
     * 名称
     */
    private String name;

    /**
     * 值  CPU=速度(MHz)
     */
    private String value;

    /**
     * 状态
     */
    private String status;

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


}
