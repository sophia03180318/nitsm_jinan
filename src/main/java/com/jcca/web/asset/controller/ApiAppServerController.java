package com.jcca.web.asset.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jcca.common.bean.ResultVo;
import com.jcca.common.bean.constant.RedisCacheConst;
import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.redis.service.RedisService;
import com.jcca.common.utils.AppLogUtils;
import com.jcca.common.utils.MyIdUtil;
import com.jcca.common.utils.ResultVoUtil;
import com.jcca.web.asset.controller.bean.AssetAppServerReq;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.asset.entity.AssetAppServer;
import com.jcca.web.asset.entity.CollectCpuLoad;
import com.jcca.web.asset.service.AssetAppServerService;
import com.jcca.web.asset.service.AssetService;
import com.jcca.web.asset.service.CollectCpuLoadService;
import com.jcca.web.asset.service.ThresholdAssetService;
import com.jcca.web.asset.vo.AssetAppServerVo;
import com.jcca.web.asset.vo.ThresholdAssetVo;
import com.jcca.web.common.service.OutService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author: hhw
 * @description: ApiAppServerController 主要是用来处理应用服务器的接口
 * @date: 2025-07-04  11:18
 * @since: 2.0.15.0
 */
@RestController
@RequestMapping("/api/appserver")
@Api(tags = "应用服务器接口")
public class ApiAppServerController {

    @Resource
    private AssetAppServerService assetAppServerService;
    @Resource
    private ThresholdAssetService thresholdAssetService;
    @Resource
    private AssetService assetService;
    @Resource
    private OutService outService;
    @Resource
    private CollectCpuLoadService collectCpuLoadService;
    @Resource
    private RedisService redisService;

    @GetMapping("/getAppServerList")
    @ApiOperation(value = "获取全部应用服务器列表")
    public ResultVo<Object> getAppServerList() {

        List<AssetAppServerVo> list = assetAppServerService.getAppServerList();

        return ResultVoUtil.success(list);
    }

    @GetMapping("/getAssetInfo")
    @ApiOperation(value = "获取应用服务器信息")
    public ResultVo<Object> getAssetInfo(@RequestParam String assetId) {
        if (StringUtils.isEmpty(assetId)) {
            return ResultVoUtil.error("缺少参数");
        }

        List<AssetAppServerVo> list = assetAppServerService.getAppServerInfo(assetId);

        return ResultVoUtil.success(list);
    }

    @GetMapping("/asset/link")
    @ApiOperation(value = "获取应用服务器连接列表")
    public ResultVo<Object> getAssetLink(@RequestParam String assetId) {
        if (StringUtils.isEmpty(assetId)) {
            return ResultVoUtil.error("缺少参数");
        }
        List<AssetAppServerVo> list = assetAppServerService.getAppServerListByAssetId(assetId);

        Map<String, Object> map = new HashMap<>();
        map.put("linkList", list);

        Asset asset = assetService.getById(assetId);
        if (asset != null && asset.getServiceType() != null && asset.getServiceType() == 1) {
            CollectCpuLoad load = collectCpuLoadService.getLastRecordByAssetId(assetId);
            map.put("cpuLoad", load);
        }

        return ResultVoUtil.success(map);
    }

    @PostMapping("/setPortLoad")
    @ApiOperation(value = "配置端口和负载阈值")
    public ResultVo<Object> setPortLoad(@RequestBody @Validated AssetAppServerReq assetAppServerReq) {
        String assetId = assetAppServerReq.getAssetId();
        Asset asset = assetService.getById(assetId);
        if (Objects.isNull(asset)) {
            return ResultVoUtil.error("资产[" + assetId + "]不存在");
        }

        AppLogUtils.buildLogInfo(LogFunctionEnum.APP_SERVER_LINK, "配置端口和负载阈值", assetAppServerReq);

        Double cpuLoad = assetAppServerReq.getCpuLoad();
        if (Objects.isNull(cpuLoad) || cpuLoad <= 0) {
            cpuLoad = 1D;
        }
        redisService.remove(RedisCacheConst.THRESHOLD_ALARM_PRE + assetId);
        thresholdAssetService.setCpuLoad(asset, cpuLoad);

        boolean flag = false;
        String serverPort = assetAppServerReq.getServerPort();
        if (serverPort.contains("，")) {
            serverPort = serverPort.replaceAll("，", ",");
        }
        String[] portArr = serverPort.split(",");
        for (String port : portArr) {
            if (StringUtils.isEmpty(port)) {
                continue;
            }
            List<AssetAppServer> list = assetAppServerService.findByAssetIdAndServerPort(assetId, port);
            if (!list.isEmpty()) {
                continue;
            }
            AssetAppServer assetAppServer = new AssetAppServer();
            assetAppServer.setId(MyIdUtil.getId());
            assetAppServer.setAssetId(assetId);
            assetAppServer.setAssetName(asset.getName());
            assetAppServer.setServerPort(Integer.parseInt(port));
            assetAppServerService.save(assetAppServer);

            flag = true;
        }
        if (flag) {
            outService.notifyOnChange(2, asset);
        }

        return ResultVoUtil.success();
    }

    @PostMapping("/delete/port")
    @ApiOperation(value = "删除端口")
    public ResultVo<Object> deletePort(@RequestBody AssetAppServerReq req) {
        String assetId = req.getAssetId();
        String serverPort = req.getServerPort();
        if (StringUtils.isEmpty(assetId) || StringUtils.isEmpty(serverPort)) {
            return ResultVoUtil.error("缺少参数");
        }
        assetAppServerService.deleteServerPort(assetId, Integer.parseInt(serverPort));

        Asset asset = assetService.getById(assetId);
        outService.notifyOnChange(2, asset);

        AppLogUtils.buildLogInfo(LogFunctionEnum.APP_SERVER_LINK, "删除应用服务器端口", req);

        return ResultVoUtil.success();
    }

    @GetMapping("/port/view")
    @ApiOperation(value = "回显配置数据")
    public ResultVo<Object> portView(@RequestParam String assetId) {

        List<AssetAppServer> list = assetAppServerService.findByAssetIdNullLink(assetId);
        Set<Integer> collect = list.stream().map(AssetAppServer::getServerPort).collect(Collectors.toSet());
        String portStr = collect.stream().map(String::valueOf).collect(Collectors.joining(","));

        ThresholdAssetVo threshold = thresholdAssetService.findAssetThreshold(assetId);

        Map<String, Object> map = new HashMap<>();
        map.put("serverPort", portStr);
        map.put("cpuLoad", 1);
        if (Objects.nonNull(threshold)) {
            map.put("cpuLoad", threshold.getCpuLoad() == null ? 1D : threshold.getCpuLoad());
        }

        return ResultVoUtil.success(map);
    }

    @GetMapping("/cpuload/line")
    @ApiOperation(value = "获取CPU负载曲线")
    public ResultVo<Object> getCpuLoad(@RequestParam String assetId) {

        QueryWrapper<CollectCpuLoad> query = Wrappers.query();
        query.eq("ASSET_ID", assetId);
        query.orderByDesc("COLLECT_TIME");
        Page<CollectCpuLoad> page = new Page<>(1, 100);
        List<CollectCpuLoad> list = collectCpuLoadService.page(page, query).getRecords();
        Collections.reverse(list);
        return ResultVoUtil.success(list);
    }


}
