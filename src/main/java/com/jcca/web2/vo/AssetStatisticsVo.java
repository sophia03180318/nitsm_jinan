package com.jcca.web2.vo;

import lombok.Data;

/**
 * @author HanHW
 * @description 资产管理中统计使用
 * @className AssetStatisticsVo
 * @date 2023/12/19 15:34
 * @since 2.1.0.0
 */
@Data
public class AssetStatisticsVo {

    private String code;

    private String name;

    private Integer total;
}
