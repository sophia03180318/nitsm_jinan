package com.jcca.admin.biz.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcca.admin.biz.dao.StationMapper;
import com.jcca.admin.biz.entity.Station;
import com.jcca.admin.biz.service.StationService;
import com.jcca.admin.system.entity.SysOrg;
import com.jcca.admin.system.service.SysOrgService;
import com.jcca.common.bean.constant.OrgTypeConst;
import com.jcca.common.config.mybatisplus.PagePlugin;
import com.jcca.common.input.LogInputUtils;
import com.jcca.common.input.ServerTypeEnum;
import com.jcca.common.utils.UrlUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

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

    @Override
    public Map<String, Object> listV2(Station station, Integer page, Integer size) {
        QueryWrapper<Station> stationQuery = Wrappers.query();
        if (StrUtil.isNotEmpty(station.getTitle())) {
            stationQuery.like("title", station.getTitle());
        }
        if (StrUtil.isNotEmpty(station.getIp())) {
            stationQuery.and(wq -> wq.eq("ip", station.getIp())
                    .or()
                    .eq("ip2", station.getIp()));
        }
        String orgTreeId = station.getOrgTreeId();
        if (StrUtil.isNotEmpty(orgTreeId)) {
            QueryWrapper<SysOrg> query = Wrappers.query();
            query.like("PIDS", "[" + orgTreeId + "]");
            query.eq("TYPE", OrgTypeConst.STATION);
            List<SysOrg> list = sysOrgService.list(query);
            List<String> collect = list.stream().map(SysOrg::getId).collect(Collectors.toList());
            stationQuery.in("ORG_ID", collect);
        }

        IPage<Station> iPage = PagePlugin.startPageT(page, size, Station.class);
        IPage<Station> page1 = this.page(iPage, stationQuery);
        List<Station> records = page1.getRecords();
        long total = page1.getTotal();
        Map<String, Object> map = new HashMap<>();
        map.put("list", records);
        map.put("total", total);
        return map;
    }


    @Override
    public String sendGetToStation(String uri, String stationId, String reqBody) {
        Station station = getById(stationId);
        if(Objects.isNull(station)){
            log.error("车站ID"+stationId+"不存在！");
            return "车站ID"+stationId+"不存在！";
        }
        List<String> stationIpList = new ArrayList<String>();
        stationIpList.add(station.getIp());
        if(StrUtil.isNotEmpty(station.getIp2())){
            stationIpList.add(station.getIp2());
        }

        for (String ip : stationIpList) {
            String url = UrlUtil.getStationUrlPrefix(station.getProxyUrl(), ip, station.getPort().toString())+uri;
            if(LogInputUtils.inputInfo(ServerTypeEnum.STATION_AGENT)){
                log.info("车站采集器客户端请求："+url+"参数："+reqBody);
            }
            try {
                String body = HttpRequest.get(url).setConnectionTimeout(5 * 1000).setReadTimeout(180 * 1000).execute().body();
                if(LogInputUtils.inputInfo(ServerTypeEnum.STATION_AGENT)){
                    log.info("车站采集器客户端请求："+url+"响应："+body);
                }
                return body;
            }catch (Exception e){
                log.error(String.format("向车站%s发送消息：%s,发生异常，异常信息：%s,将尝试其他IP通讯。",station.getTitle(),reqBody,e.toString()));
            }
        }
        log.error(String.format("向车站%s发送消息：%s,通讯失败。",station.getTitle(),reqBody));
        return String.format("向车站%s发送消息：%s,通讯失败。",station.getTitle(),reqBody);
    }


}