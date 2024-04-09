package com.jcca.web2.dto;

import lombok.Data;

/**
 * @author HanHW
 * @description 资产阈值
 * @className ThresholdDto
 * @date 2023/12/15 16:44
 * @since 2.1.0.0
 */
@Data
public class ThresholdDto {

    /**
     * 组织ID
     */
    private String orgId;

    /**
     * 资产ID
     */
    private String assetId;

    /**
     * 资产类型
     */
    private Integer assetDesk;

    /**
     * 业务类型ID
     */
    private String serviceTypeId;
    /**
     * 阈值分类 CPU，内存等
     * 字典 THRESHOLD_CATEGORY
     * 枚举 ThresholdCategoryEnum
     */
    private String category;
    /**
     * 普通阈值
     */
    private Integer general;
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
     * 0不可用，1可用
     */
    private Integer onAlarm;
}
