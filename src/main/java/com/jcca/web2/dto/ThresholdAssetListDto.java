package com.jcca.web2.dto;

import lombok.Data;

import java.util.List;

/**
 * @author HanHW
 * @description 阈值管理资产列表查询条件
 * @className ThresholdAssetListDto
 * @date 2024/1/4 15:11
 * @since 2.1.0.0
 */
@Data
public class ThresholdAssetListDto {

    private List<String> orgIds;

    private Integer assetDesk;

    private String serviceTypeId;

    private String orgId;

    private String category;

}
