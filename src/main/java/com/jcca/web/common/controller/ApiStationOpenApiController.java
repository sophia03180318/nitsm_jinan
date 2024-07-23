package com.jcca.web.common.controller;


import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jcca.admin.biz.entity.Station;
import com.jcca.admin.biz.service.StationService;
import com.jcca.common.bean.RestBean;
import com.jcca.common.bean.constant.StatusConst;
import com.jcca.common.config.thymeleaf.utility.DictUtil;
import com.jcca.common.utils.ValidatorUtils;

import com.jcca.web.asset.entity.*;

import com.jcca.web.asset.service.*;

import com.jcca.web.common.controller.bean.*;

import com.jcca.web.common.controller.req.StationAlarmReqV2;
import com.jcca.web.common.controller.req.StationAlarmResp;
import com.jcca.web.common.service.StationAlarmService;

import com.jcca.web2.dto.ThresholdManageQuery;
import com.jcca.web2.service.ThresholdManageService;
import com.jcca.web2.vo.ThresholdManageVo;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @description: 为车站采集器开放的接口
 * @author: Lvyp
 * @create: 2024/04/10 14:19
 */
@RestController
@RequestMapping("/api/free/station/openApi")
public class ApiStationOpenApiController {

    @Resource
    private AssetService assetService;
    @Resource
    private StationService stationServ;
    @Resource
    private CabinetService cabinetServ;
    @Resource
    private AssetAttachService assetAttachService;
    @Resource
    private ThresholdManageService thresholdManageServ;

    @Resource
    private ThresholdProcessService processServ;
    @Resource
    private StationAlarmService stationAlarmService;




    @PostMapping("/pushAlarmInfo")
    RestBean  pushAlarmInfo(@RequestBody StationAlarmReqV2 req){
        String validator = ValidatorUtils.validateReq(req);
        if(StrUtil.isNotEmpty(validator)){
            return RestBean.ofError(validator);
        }
        StationAlarmResp stationAlarmResp = stationAlarmService.disposeStationAlarm(req);

        return RestBean.ofSuccess(stationAlarmResp);
    }





    /**
     * 获取车站资产
     * @return
     */
    @GetMapping("/getAsset")
    RestBean getAsset(String stationIp){
        List<Station> stationList = stationServ.findByIp(stationIp);
        if(stationList.isEmpty()){
            return RestBean.ofError("系统不存在此车站配置，ip："+stationIp);
        }
        QueryWrapper<Asset> assetQueryWrapper = new QueryWrapper<>();
        assetQueryWrapper.select("id","NAME","ASSET_CODE","ASSET_MODE","DESK","IP","IP2","STATUS","WATCH",
                "COLLECTION_TYPE","OS_USER","OS_PASSWORD","LOGIN_NAME","LOGIN_PWD","LOGIN_PORT","MANUFACTURER_ID","ntp_flag","show_topo","asset_image");
        assetQueryWrapper.eq("ORG_ID",stationList.get(0).getOrgId());
        assetQueryWrapper.eq("IS_DEL", StatusConst.OK);
        List<Asset> list = assetService.list(assetQueryWrapper);
        for (Asset asset : list) {
            AssetAttach assetAttach = assetAttachService.getByAssetId(asset.getId());
            if(Objects.nonNull(assetAttach)){
                asset.setCabinetId(assetAttach.getCabinetId());
                asset.setStartPosition(assetAttach.getStartPosition());
                asset.setEndPosition(assetAttach.getEndPosition());
            }
        }

        return RestBean.ofSuccess(list);
    }

    /**
     * 获取机柜信息
     * @param stationIp
     * @return
     */
    @GetMapping("/getCabinet")
    RestBean getCabinet(String stationIp){
        List<Station> stationList = stationServ.findByIp(stationIp);
        if(stationList.isEmpty()){
            return RestBean.ofError("系统不存在此车站配置，ip："+stationIp);
        }

        List<Cabinet> cabinetList = cabinetServ.findByOrgId(stationList.get(0).getOrgId());
        return RestBean.ofSuccess(cabinetList);
    }

    /**
     * 获取设备厂家
     */
    @GetMapping("/getFactory")
    RestBean getFactory(){
        //字典获取
        List<ItsmAssetFactoryResp> respList = new ArrayList<>();
        Map<String, String> assetFactory = DictUtil.value("ASSET_FACTORY");
        Set<String> keySet = assetFactory.keySet();
        for (String code : keySet) {
            ItsmAssetFactoryResp resp = new ItsmAssetFactoryResp();
            resp.setCode(code);
            resp.setName(assetFactory.get(code));
            respList.add(resp);
        }

        return RestBean.ofSuccess(respList);
    }

    /**
     * 获取设备型号
     */
    @GetMapping("/getImage")
    RestBean getImage(){
        //字典获取
        List<ItsmAssetImageResp> respList = new ArrayList<>();
        Map<String, String> assetFactory = DictUtil.value("ASSET_IMAGE");
        Set<String> keySet = assetFactory.keySet();
        for (String code : keySet) {
            ItsmAssetImageResp resp = new ItsmAssetImageResp();
            resp.setAssetMode(assetFactory.get(code));
            resp.setName(code);
            respList.add(resp);
        }

        return RestBean.ofSuccess(respList);

    }

    /**
     * 获取设备类型
     */
    @GetMapping("/getAssetMode")
    RestBean getAssetMode(){
        //字典获取
        List<ItsmAssetModeResp> respList = new ArrayList<>();
        Map<String, String> assetFactory = DictUtil.value("ASSET_MODE");
        Set<String> keySet = assetFactory.keySet();
        for (String code : keySet) {
            ItsmAssetModeResp resp = new ItsmAssetModeResp();
            resp.setAssetMode(code);
            resp.setName(assetFactory.get(code));
            respList.add(resp);
        }

        return RestBean.ofSuccess(respList);

    }

    /**
     * 获取告警配置信息
     */
    @GetMapping("/getAlarmConf")
    RestBean getAlarmConf(String stationIp){
        if(StrUtil.isEmpty(stationIp)){
            return RestBean.ofError("缺少车站IP");
        }
        List<Station> stationList = stationServ.findByIp(stationIp);
        if(stationList.isEmpty()){
            return RestBean.ofError("系统不存在此车站配置，ip："+stationIp);
        }
        String orgId = stationList.get(0).getOrgId();

        List<Asset> assets = assetService.ListByOrgIds(Arrays.asList(orgId));
        if(Objects.isNull(assets)||assets.isEmpty()){
            return RestBean.ofSuccess(new ArrayList<>());
        }

        List<String> assetIdList = assets.stream().map(item -> item.getId()).collect(Collectors.toList());


        Map<String,ItsmAlarmRuleResp> catchMap = new HashMap<>();

        //查询阈值配置
        ThresholdManageQuery manageQuery = new ThresholdManageQuery();
        manageQuery.setOrgId(orgId);
        List<ThresholdManageVo> thresholdManageVos = thresholdManageServ.getList(manageQuery);
        for (ThresholdManageVo item : thresholdManageVos) {
            String assetId = item.getAssetId();

            ItsmAlarmRuleResp itsmAlarmRuleResp = catchMap.get(assetId);
            if(Objects.isNull(itsmAlarmRuleResp)){
                itsmAlarmRuleResp = new ItsmAlarmRuleResp();
            }

            List<ItsmThresholdConf> thresholdConfList = itsmAlarmRuleResp.getThresholdConfList();
            if(Objects.isNull(thresholdConfList)||thresholdConfList.isEmpty()){
                thresholdConfList = new ArrayList<>();
            }

            ItsmThresholdConf conf = new ItsmThresholdConf();
            conf.setCategory(item.getCategory());
            conf.setGeneral(item.getGeneral());
            conf.setRangeMin(item.getRangeMin());
            conf.setRangeMax(item.getRangeMax());
            conf.setStepHigh(item.getStepHigh());
            conf.setStepHigher(item.getStepHigher());
            conf.setStepHighest(item.getStepHighest());

            thresholdConfList.add(conf);

            itsmAlarmRuleResp.setThresholdConfList(thresholdConfList);

            catchMap.put(assetId,itsmAlarmRuleResp);
        }


        //查询组织下资产配置的所有进程
        List<ThresholdProcess> thresholdProcesses = processServ.selectByAssetList(assetIdList);
        Map<String, List<ThresholdProcess>> collect = thresholdProcesses.stream().collect(Collectors.groupingBy(ThresholdProcess::getAssetId));
        Set<String> keySet = collect.keySet();
        for (String assetId : keySet) {
            ItsmAlarmRuleResp resp = catchMap.get(assetId);
            if(Objects.isNull(resp)){
                resp = new ItsmAlarmRuleResp();
                resp.setAssetId(assetId);
            }
            List<ThresholdProcess> thresholdProcessesList = collect.get(assetId);
            List<ItsmProcessConf> confList = new ArrayList<>();
            for (ThresholdProcess thresholdProcess : thresholdProcessesList) {
                ItsmProcessConf conf = new ItsmProcessConf();
                if(StrUtil.isNotEmpty(thresholdProcess.getThresholdMemory())){
                    conf.setMemoryRate(Double.valueOf(thresholdProcess.getThresholdMemory()));
                }
                if(StrUtil.isNotEmpty(thresholdProcess.getThresholdCpu())){
                    conf.setCpuRate(Double.valueOf(thresholdProcess.getThresholdCpu()));
                }
                conf.setProcessId(thresholdProcess.getProcessId());
                conf.setProcessName(thresholdProcess.getProcessName());
                confList.add(conf);
            }

            resp.setProcessList(confList);
            catchMap.put(assetId,resp);
        }

        //全量高进相关配置信息
        Collection<ItsmAlarmRuleResp> values = catchMap.values();

        return RestBean.ofSuccess(JSONUtil.toJsonStr(values));
    }
}
