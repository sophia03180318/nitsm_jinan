package com.jcca.web2.vo;

import lombok.Data;

import java.util.List;

/**
 * 车站IP网段信息
 * @description: 车站IP网段信息
 * @author: Lvyp
 * @create: 2024/03/01 16:59
 */
@Data
public class StationNetObj {

    /**
     * 车站ID
     */
    private String stationOrgId;
    /**
     * 车站名称
     */
    private String stationName;
    /**
     * 网段信息
     */
    private List<NetObj> netWorkAddressList;



}
