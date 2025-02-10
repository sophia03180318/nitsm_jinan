package com.jcca.web2.dto;

import lombok.Data;

import java.util.List;

/**
 * @author HanHW
 * @description 阈值管理查询
 * @className ThresholdManageQuery
 * @date 2024/1/4 17:43
 * @since 2.1.0.0
 */
@Data
public class ThresholdManageQuery {

    private String orgId;
    private List<String> orgIds;

    private String assetId;
    /**
     * 资产类型
     */
    private Integer assetDesk;
    /**
     * 阈值分类 CPU，内存等
     * 字典 THRESHOLD_CATEGORY
     * 枚举 ThresholdCategoryEnum
     */
    private String category;

    /**
     * 业务类型ID
     */
    private String serviceTypeId;

    private String assetName;

    private String assetIp;

    private String assetImage;
    /**
     * 1组织，2单个
     * ThresholdAutoFlagEnum
     */
    private Integer autoFlag;

}
