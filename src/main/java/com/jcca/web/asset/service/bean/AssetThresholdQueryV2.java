package com.jcca.web.asset.service.bean;

import com.jcca.web.asset.enums.ThresholdSectionEnum;
import lombok.Data;

/**
 * @description: 查询资产相关的各种类型的阈值入参
 * @author: Lvyp
 * @create: 2023/11/07 11:43
 */
@Data
public class AssetThresholdQueryV2 {
    /**
     * 设备ID
     */
    private String assetId;
    /**
     * 普通阈值取值的属性名称
     */
    private String property;
    /**
     * 区间阈值类型
     */
    private ThresholdSectionEnum threshold;
    /**
     * 区间阈值的具体类型标记：如磁盘名称、端口名称
     */
    private String flag;

}
