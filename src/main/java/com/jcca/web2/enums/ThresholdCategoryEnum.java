package com.jcca.web2.enums;

import org.springframework.util.StringUtils;

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
    CPU("CPU阈值"),
    /**
     * 内存阈值
     */
    MEMORY("内存阈值"),
    /**
     * 磁盘阈值
     */
    DISK("磁盘阈值"),
    /**
     * 流入丢包率
     */
    PACKET_LOSS_IN("流入丢包率"),
    /**
     * 流出丢包率
     */
    PACKET_LOSS_OUT("流出丢包率"),
    /**
     * 流入误码率
     */
    CODE_ERROR_IN("流入误码率"),
    /**
     * 流出误码率
     */
    CODE_ERROR_OUT("流出误码率"),
    /**
     * 时间偏差
     */
    TIME_DEVIATION("时间偏差"),
    /**
     * 表空间使用率
     */
    TABLE_SPACE("表空间使用率"),
    /**
     * 流入率
     */
    PORT_RATE_IN("流入率"),
    /**
     * 流出率
     */
    PORT_RATE_OUT("流出率"),
    /**
     * 连接数阈值
     */
    CONNECTION_NO("连接数阈值"),
    /**
     * 运行时长
     */
    RUNNINGTIME_DEVIATION("运行时长"),
    /**
     * 温度
     */
    TEMPERATURE("温度"),
    /**
     * 交换机接收光功率
     */
    SWITCH_OPTICAL_RX("交换机接收光功率"),
    /**
     * 交换机发送光功率
     */
    SWITCH_OPTICAL_TX("交换机发送光功率"),
    /**
     * CPU负载阈值
     */
    CPU_LINUX_LOAD_AVG("CPU负载"),

    ;

    String title;

    ThresholdCategoryEnum(String title) {
        this.title = title;
    }

    public static String getTitle(String name) {
        if (StringUtils.isEmpty(name)) {
            return name;
        }
        ThresholdCategoryEnum categoryEnum = ThresholdCategoryEnum.valueOf(name);
        return categoryEnum.title;
    }
}
