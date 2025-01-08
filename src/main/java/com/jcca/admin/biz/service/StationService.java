package com.jcca.admin.biz.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jcca.admin.biz.entity.Station;

import java.util.List;
import java.util.Map;

/**
 * @author hanwone
 * @date 2020-06-19 11:08:31
 **/
public interface StationService extends IService<Station> {


    /**
     * 查询车站
     *
     * @param stationIp
     */
    List<Station> findByIp(String stationIp);


    /**
     * 根据车站IP获取车站需要的详情
     *
     * @param stationIp
     * @return
     */
    Map<String, Object> getstationInfoByIp(String stationIp);


    /**
     * 发送消息到车站
     *
     * @param uri
     * @param stationId
     * @param reqBody
     * @return
     */
    String sendPostToStation(String uri, String stationId, String reqBody);

    Map<String, Object> listV2(Station station, Integer page, Integer size);

}
