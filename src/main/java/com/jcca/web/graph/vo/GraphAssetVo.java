package com.jcca.web.graph.vo;

import lombok.Data;

/**
 * @ClassName GraphAssetVo
 * @Description 拓扑图搜索
 * @Date 2020/7/22 14:08
 * @Author hanwone
 */
@Data
public class GraphAssetVo {
    /**
     * 设备ID
     */
    private String assetId;
    /**
     * 设备名称
     */
    private String assetName;
}
