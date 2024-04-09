package com.jcca.web2.vo;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author HanHW
 * @description 采集配置
 * @className CollectConfigVo
 * @date 2024/2/28 15:05
 * @since 2.1.0.0
 */
@Data
public class CollectConfigVo {

    /**
     * 类型
     */
    @NotNull(message = "资产类型不能为空")
    private Integer assetMode;
    /**
     * 型号
     */
    @NotEmpty(message = "资产型号不能为空")
    private String assetImage;
    /**
     * 厂商ID
     */
    @NotEmpty(message = "资产厂商不能为空")
    private String manufactureId;
    /**
     * 采集类型
     */
    @NotNull(message = "采集类型不能为空")
    private Integer systemType;
    /**
     * 指标数据ID
     */
    private List<String> targetIds;

}
