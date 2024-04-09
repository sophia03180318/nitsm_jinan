package com.jcca.admin.biz.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcca.admin.biz.dao.StationMapper;
import com.jcca.admin.biz.entity.Station;
import com.jcca.admin.biz.service.StationService;
import com.jcca.admin.system.service.SysOrgService;
import com.jcca.common.input.LogInputUtils;
import com.jcca.common.input.ServerTypeEnum;
import com.jcca.common.utils.UrlUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

/**
 * @author hanwone
 * @date 2020-06-19 11:08
 **/
@Slf4j
@Service
public class StationServiceImpl extends ServiceImpl<StationMapper, Station> implements StationService {

    @Resource
    private SysOrgService sysOrgService;

    @Override
    public List<Station> findByIp(String stationIp) {
        QueryWrapper<Station> queryWrapper = new QueryWrapper<Station>();
        queryWrapper.eq("IP", stationIp).or().eq("IP2", stationIp);
        return list(queryWrapper);
    }

    @Override
    public Map<String, Object> getstationInfoByIp(String stationIp) {
        Map<String, Object> result = new HashMap<>();
        List<Station> list = this.findByIp(stationIp);
        if (CollUtil.isEmpty(list)) {
            return null;
        }
        Station station = list.get(0);
        String orgPathName = sysOrgService.getParentPathName(station.getOrgId());
        result.put("orgPathName", orgPathName);
        result.put("orgName", station.getTitle());
        result.put("proxyUrl", station.getProxyUrl());
        result.put("ip", station.getIp());
        result.put("port", station.getPort());

        return result;
    }

    @Override
    public String sendPostToStation(String uri, String stationId, String reqBody) {
        Station station = getById(stationId);
        if (Objects.isNull(station)) {
            log.error("向车站发送消息时车站ID[{}]不存在！", stationId);
            return "车站ID[" + stationId + "]不存在！";
        }
        List<String> stationIpList = new ArrayList<String>();
        stationIpList.add(station.getIp());
        if (StrUtil.isNotEmpty(station.getIp2())) {
            stationIpList.add(station.getIp2());
        }

        for (String ip : stationIpList) {
            String url = UrlUtil.getStationUrlPrefix(station.getProxyUrl(), ip, station.getPort().toString()) + uri;
            if (LogInputUtils.inputInfo(ServerTypeEnum.STATION_AGENT)) {
                log.info("车站采集器客户端请求：{} 参数：{}", url, reqBody);
            }
            try {
                String body = HttpUtil.createPost(url).body(reqBody, "application/json").setConnectionTimeout(5 * 1000).setReadTimeout(300 * 1000).execute().body();
                if (LogInputUtils.inputInfo(ServerTypeEnum.STATION_AGENT)) {
                    log.info("车站采集器客户端请求：{} 响应：{}", url, body);
                }
                return body;
            } catch (Exception e) {
                log.error(String.format("向车站%s发送消息：%s,发生异常，异常信息：%s,将尝试其他IP通讯。", station.getTitle(), reqBody, e.toString()));
            }
        }
        return String.format("向车站%s发送消息：%s,通讯失败。", station.getTitle(), reqBody);
    }
}