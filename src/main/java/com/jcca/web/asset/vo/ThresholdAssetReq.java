package com.jcca.web.asset.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;

/**
 * @ClassName ThresholdAssetVo
 * @Description
 * @Date 2020/5/21 15:25
 * @Author hanwone
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ThresholdAssetReq extends ThresholdAssetVo {
    /**
     * 资产ID
     */
    @NotNull(message = "资产ID不能为空")
    private String assetId;
}
