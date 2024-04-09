package com.jcca.web.common.controller.req;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @ClassName AssetOutReq
 * @Description 外部请求资产列表
 * @Date 2020/7/30 16:54
 * @Author hanwone
 */
@Data
@NoArgsConstructor
public class AssetOutReq {
    /**
     * 资产ID
     */
    private String assetId;
    /**
     * 需求类型 2-获取中心资产，4-获取车站资产
     */
    private Integer type;
    /**
     * 车站采集器IP
     */
    private String stationIp;
    /**
     * 车站分组
     */
    private String stationGroup;

    /**
     * 是否同步不监控资产，0不同步，1同步
     */
    private Integer watchMode;

    public AssetOutReq(Integer type) {
        this.type = type;
    }
}
