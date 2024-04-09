package com.jcca.web2.enums;

/**
 * @author HanHW
 * @description 阈值类型
 * @className ThresholdCategoryEnum
 * @date 2023/12/15 14:56
 * @since 2.1.0.0
 */
public enum ThresholdCategoryEnum {

    /**
     * CPU阈值
     */
    CPU,
    /**
     * 内存阈值
     */
    MEMORY,
    /**
     * 磁盘阈值
     */
    DISK,
    /**
     * 流入丢包率
     */
    PACKET_LOSS_IN,
    /**
     * 流出丢包率
     */
    PACKET_LOSS_OUT,
    /**
     * 流入误码率
     */
    CODE_ERROR_IN,
    /**
     * 流出误码率
     */
    CODE_ERROR_OUT,
    /**
     * 时间偏差
     */
    TIME_DEVIATION,
    /**
     * 表空间使用率
     */
    TABLE_SPACE,
    /**
     * 流入率
     */
    PORT_RATE_IN,
    /**
     * 流出率
     */
    PORT_RATE_OUT,
    /**
     * 连接数阈值
     */
    CONNECTION_NO,
    /**
     * 运行时长
     */
    RUNNINGTIME_DEVIATION,
    /**
     * 温度
     */
    TEMPERATURE,
    /**
     * 交换机接收光功率
     */
    SWITCH_OPTICAL_RX,
    /**
     * 交换机发送光功率
     */
    SWITCH_OPTICAL_TX,

    ;

}
