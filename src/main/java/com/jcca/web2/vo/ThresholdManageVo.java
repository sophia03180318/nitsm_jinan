package com.jcca.web2.vo;

import lombok.Data;

/**
 * @author HanHW
 * @description 阈值管理返回值
 * @className ThresholdManageVo
 * @date 2024/1/4 17:46
 * @since 2.1.0.0
 */
@Data
public class ThresholdManageVo {

    private String id;
    private String assetName;
    private String assetId;
    private String assetIp;
    private String deskStr;
    private String assetDesk;
    private String orgName;
    private String orgId;
    /**
     * 业务类型ID
     */
    private String serviceTypeStr;
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
     * 1批量，2单个
     */
    private Integer autoFlag;
}
