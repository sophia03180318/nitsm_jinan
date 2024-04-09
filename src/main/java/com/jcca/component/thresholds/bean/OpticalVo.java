package com.jcca.component.thresholds.bean;

import lombok.Data;

/**
 * @Description 光交换机光功率数据类
 * @ClassName OpticalVo
 * @Date 2022/6/16 9:48
 * @Author hanwone
 * @Since 2.0.0.1
 */
@Data
public class OpticalVo {

    /**
     * 端口号
     */
    private String intPort;

    /**
     * 传输功率 dBm
     */
    private String txPower;

    /**
     * 接收功率 dBm
     */
    private String rxPower;
}
