package com.jcca.web.asset.vo;

import lombok.Data;

import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;

/**
 * @ClassName ThresholdAssetVo
 * @Description
 * @Date 2020/5/21 15:25
 * @Author hanwone
 */
@Data
public class ThresholdAssetVo {
    /**
     * 资产类型
     */
    @NotNull
    private Integer assetMode;

    private String assetId;
    /**
     * CPU使用率
     */
    @Digits(integer = 2, fraction = 2, message = "CPU使用率只能输入2位整数最多两位小数")
    private Double cpu;
    /**
     * 磁盘使用率
     */
    @Digits(integer = 2, fraction = 2, message = "磁盘使用率只能输入2位整数最多两位小数")
    private Double disk;
    /**
     * 内存使用率
     */
    @Digits(integer = 2, fraction = 2, message = "内存使用率只能输入2位整数最多两位小数")
    private Double memory;
    /**
     * 接收丢包率
     */
    @Digits(integer = 2, fraction = 2, message = "丢包率只能输入2位整数最多两位小数")
    private Double packetLossIn;
    /**
     * 发送丢包率
     */
    @Digits(integer = 2, fraction = 2, message = "丢包率只能输入2位整数最多两位小数")
    private Double packetLossOut;
    /**
     * 接收误码率
     */
    @Digits(integer = 2, fraction = 2, message = "误码率只能输入2位整数最多两位小数")
    private Double codeErrorIn;
    /**
     * 发送误码率
     */
    @Digits(integer = 2, fraction = 2, message = "误码率只能输入2位整数最多两位小数")
    private Double codeErrorOut;
    /**
     * 端口流入百分比
     */
    @Digits(integer = 2, fraction = 2, message = "端口流入率只能输入2位整数最多两位小数")
    private Double portRateIn;
    /**
     * 端口流出百分比
     */
    @Digits(integer = 2, fraction = 2, message = "端口流出率只能输入2位整数最多两位小数")
    private Double portRateOut;
    /**
     * 时间偏差
     */
    @Digits(integer = 10, fraction = 0, message = "时间偏差最多只能输入10位整数")
    private Integer timeDeviation;
    /**
     * Linux运行时长
     */
    @Digits(integer = 10, fraction = 0, message = "Linux运行时长为不超过十位数的正整数")
    private Integer runningTimeDeviationLinux;
    /**
     * AIX运行时长
     */
    @Digits(integer = 10, fraction = 0, message = "Aix运行时长为不超过十位数的正整数")
    private Integer runningTimeDeviationAix;

    /**
     * 运行时长
     */
    @Digits(integer = 10, fraction = 0, message = "运行时长为不超过十位数的正整数")
    private Integer runningTimeDeviation;

    /**
     * Windows运行时长
     */
    @Digits(integer = 10, fraction = 0, message = "Windows运行时长为不超过十位数的正整数")
    private Integer runningTimeDeviationWindows;

    /**
     * 表空间使用率
     */
    @Digits(integer = 2, fraction = 2, message = "表空间使用率只能输入2位整数最多两位小数")
    private Double tablespace;
}
