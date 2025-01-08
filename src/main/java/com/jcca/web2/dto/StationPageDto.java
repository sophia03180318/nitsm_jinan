package com.jcca.web2.dto;

import lombok.Data;

/**
 * @description: 车站分页查询
 * @author: Lvyp
 * @create: 2025/01/07 10:04
 */
@Data
public class StationPageDto extends PageDto{

    /**
     * 车站名称
     */
    private String stationName;
    /**
     * 车站IP
     */
    private String stationIp;
    /**
     * 车站TAG号
     */
    private String tagNum;
    /**
     * 组织树 筛选ID
     */
    private String orgTreeId;


}
