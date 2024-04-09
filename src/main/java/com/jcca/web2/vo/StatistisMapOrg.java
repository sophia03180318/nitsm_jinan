package com.jcca.web2.vo;

import lombok.Data;

import java.util.List;

/**
 * @author HanHW
 * @description 大屏中的线路及车站分组
 * @className StatistisMapOrgV2
 * @date 2023/11/3 15:05
 * @since 2.1.0.0
 */
@Data
public class StatistisMapOrg {

    private MapStationVo line;

    private List<MapStationVo> stations;
}
