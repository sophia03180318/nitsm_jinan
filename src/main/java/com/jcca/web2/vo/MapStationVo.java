package com.jcca.web2.vo;

import lombok.Data;

/**
 * @author HanHW
 * @description 车站地图数据
 * @className MapStationVo
 * @date 2023/11/9 18:13
 * @since 2.1.0.0
 */
@Data
public class MapStationVo {

    /**
     * 组织ID
     */
    private String id;
    /**
     * 组织名称
     */
    private String name;

    // 地图上车站x坐标
    private String stationx;
    // 地图上车站y坐标
    private String stationy;
}
