package com.jcca.web.common.controller.bean;

import lombok.Data;

/**
 * @description: 阈值配置
 * @author: Lvyp
 * @create: 2024/04/19 18:24
 */
@Data
public class ItsmThresholdConf {

    /**
     * 阈值的类型
     * ThresholdCategoryEnum
     */
    private String category;
    /**
     * 普通阈值
     */
    private Double general;
    /**
     * 范围阈值下限
     */
    private Double rangeMin;
    /**
     * 范围阈值上限
     */
    private Double rangeMax;
    /**
     * 阶梯阈值高
     */
    private Double stepHigh;
    /**
     * 阶梯阈值较高
     */
    private Double stepHigher;
    /**
     * 阶梯阈值最高
     */
    private Double stepHighest;
    /**
     * 标记
     */
    private  String flag;

}
