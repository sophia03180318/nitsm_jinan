package com.jcca.web2.dto;

import lombok.Data;

/**
 * @author HanHW
 * @description 采集配置数据
 * @className CollectConfigDto
 * @date 2024/2/28 14:49
 * @since 2.1.0.0
 */
@Data
public class CollectConfigDto {

    /**
     * 类型
     */
    private Integer assetMode;

    /**
     * 型号
     */
    private String assetImage;
}
