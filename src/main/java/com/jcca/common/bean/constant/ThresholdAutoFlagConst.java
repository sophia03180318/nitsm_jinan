package com.jcca.common.bean.constant;

/**
 * @ClassName ThresholdAutoFlagConst
 * @Description 阈值类型标记
 * @Date 2020/8/24 10:15
 * @Author hanwone
 */
public interface ThresholdAutoFlagConst {
    /**
     * 组织级别阈值
     */
    byte ORG_THRESHOLD = 1;

    /**
     * 设备类型级别阈值
     */
    byte MODE_THRESHOLD = 3;

    /**
     * 单个设备阈值
     */
    byte SIGNLE_THRESHOLD = 2;
}
