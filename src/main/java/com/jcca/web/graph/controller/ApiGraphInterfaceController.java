package com.jcca.web.graph.controller;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.jcca.admin.biz.controller.GraphInterfaceController;
import com.jcca.admin.system.service.TopoAssetPortService;
import com.jcca.admin.system.vo.AssetPortVo;
import com.jcca.common.bean.ResultVo;
import com.jcca.common.bean.constant.RedisCacheConst;
import com.jcca.common.redis.service.RedisService;
import com.jcca.common.utils.ResultVoUtil;
import com.jcca.common.utils.SpringContextUtil;
import com.jcca.component.thresholds.bean.OpticalSwitchBean;
import com.jcca.component.thresholds.impl.DisposeOpticalAdapterImpl;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.asset.entity.AssetHidConf;
import com.jcca.web.asset.service.AssetHidConfService;
import com.jcca.web.asset.service.AssetService;
import com.jcca.web.collect.entity.CollectInterfaces;
import com.jcca.web.collect.entity.CollectNetworkCard;
import com.jcca.web.collect.service.CollectInterfacesService;
import com.jcca.web.collect.service.CollectNetworkCardService;
import com.jcca.web.graph.util.PortTranfromUtil;
import com.jcca.web.graph.vo.StatisticsInfoVo;
import com.jcca.web.graph.vo.TopoPortIndexReq;
import com.jcca.web.graph.vo.TopoPortInfoVo;
import com.jcca.web.statistics.service.HourInterfacesService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author zhaozheng@jccatech.com
 * @date 2020/8/17 16:50
 */
@Controller
@RequestMapping("/api/graphInterface")
@Api(tags = "端口拓扑相关接口")
public class ApiGraphInterfaceController {

    private static final String SON_PORT_FLAG = ".";
    // SONET口标记
    private static final String SONET_PORT_FLAG = "SONET";

    @Resource
    private TopoAssetPortService topoAssetPortService;
    @Resource
    private HourInterfacesService hourInterfacesService;
    @Resource
    private CollectInterfacesService interfaceServ;
    @Resource
    private RedisService redisService;
    @Resource
    private AssetHidConfService hidConfService;
    @Resource
    private AssetService assetService;
    @Resource
    private CollectNetworkCardService networkCardService;

    /**
     * 获取端口信息
     *
     * @return
     */
    @GetMapping("/getPortByName")
    @ApiOperation(value = "通过名称检索端口")
    @ResponseBody
    public ResultVo getPortByName(String assetId, String portName) {

        List<CollectInterfaces> ports = interfaceServ.filterPort(assetId);
        for (CollectInterfaces port : ports) {
            if (port.getPortName().equals(portName) || port.getPortIndex().equals(portName)) {
                TopoPortIndexReq topoPortIndexReq = new TopoPortIndexReq();
                topoPortIndexReq.setAssetId(assetId);
                topoPortIndexReq.setPortName(port.getPortIndex());
                return ResultVoUtil.success(topoPortIndexReq);
            }
        }
        return ResultVoUtil.success("未查询到指定端口~");
    }


    /**
     * 获取端口信息
     *
     * @return
     */
    @GetMapping("/listPort")
    @ApiOperation(value = "获取端口拓扑图")
    @ResponseBody
    public ResultVo listPort(String assetId, String pcbId) {
        GraphInterfaceController graphInterfaceController = SpringContextUtil.getBean(GraphInterfaceController.class);
        Map map = graphInterfaceController.portList(assetId, pcbId);
        return ResultVoUtil.success(map);
    }

    @GetMapping("/listGroupPort")
    @ApiOperation(value = "获取组端口")
    @ResponseBody
    public ResultVo listGroupPort(String assetId, String pcbId) {
        GraphInterfaceController graphInterfaceController = SpringContextUtil.getBean(GraphInterfaceController.class);
        Map map = graphInterfaceController.portList(assetId, pcbId);
        List<AssetPortVo> ports = (List<AssetPortVo>) map.get("port");
        try {
            List<AssetPortVo> assetPortIntger = PortTranfromUtil.tranfromIntger(ports);
            List<AssetHidConf> hideList = hidConfService.getFlagListByAsset(assetId, AssetHidConf.TypeEnum.PORT.name());
            List<String> hidNameList = hideList.stream().map(item -> item.getFlag()).collect(Collectors.toList());
            for (AssetPortVo portVo : assetPortIntger) {
                if (hidNameList.contains(portVo.getPortSlugName())) {
                    portVo.setShow(false);
                } else {
                    portVo.setShow(true);
                }
            }
            map.put("port", assetPortIntger);
        } catch (Exception e) {
            map.put("port", ports);
        }

        return ResultVoUtil.success(map);
    }

    @GetMapping("/getSonList")
    @ApiOperation(value = "获取子端口")
    @ResponseBody
    public ResultVo getSonList(String assetId, String portIndex) {
        if (StrUtil.isEmpty(assetId) || StrUtil.isEmpty(portIndex)) {
            return ResultVoUtil.success(new ArrayList<AssetPortVo>());
        }

        List<AssetPortVo> sonList = new ArrayList<AssetPortVo>();
        if (portIndex.contains(SONET_PORT_FLAG)) {
            // 需要转换查询E开头的端口、
            portIndex = portIndex.replace(SONET_PORT_FLAG, "");
            // 查询SONET口下的虚拟端口
            sonList = topoAssetPortService.querySonetSonList(portIndex + SON_PORT_FLAG, assetId);
        } else {
            portIndex = portIndex + SON_PORT_FLAG;
            sonList = topoAssetPortService.querySonList(portIndex, assetId);
        }

        // 查询状态一小时之前是断则置为灰色
        for (AssetPortVo item : sonList) {
            Date updateDate = item.getUpdateDate();
            if (GraphInterfaceController.DOWN_STATUS.equals(item.getStatus()) && Objects.nonNull(updateDate)) {
                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.HOUR, -1);
                Date time = calendar.getTime();
                long between = DateUtil.between(updateDate, time, DateUnit.MS, false);
                if (between > 0) {
                    item.setStatus(GraphInterfaceController.UNKONW_STATUS);
                }
            }
        }
        return ResultVoUtil.success(sonList);

    }

    @PostMapping("/portInfo")
    @ApiOperation(value = "获取端口详情")
    @ResponseBody
    public ResultVo portInfo(@RequestBody TopoPortIndexReq topoPortIndexReq) {
        if (StrUtil.isEmpty(topoPortIndexReq.getPortName())) {
            return ResultVoUtil.error("未配置端口信息");
        }
        TopoPortInfoVo topoPortInfoVo = topoAssetPortService.selectPortIndex(topoPortIndexReq.getAssetId(),
                topoPortIndexReq.getPortName());
        if (Objects.isNull(topoPortInfoVo)) {
            return ResultVoUtil.error("未找到端口信息");
        }
        List<StatisticsInfoVo> portIns = hourInterfacesService.portIns(topoPortIndexReq.getAssetId(),
                topoPortIndexReq.getPortName());
        List<StatisticsInfoVo> portOuts = hourInterfacesService.portOuts(topoPortIndexReq.getAssetId(),
                topoPortIndexReq.getPortName());
        List<StatisticsInfoVo> discardPackageIns = hourInterfacesService
                .discardPackageIns(topoPortIndexReq.getAssetId(), topoPortIndexReq.getPortName());
        List<StatisticsInfoVo> discardPackageOuts = hourInterfacesService
                .discardPackageOuts(topoPortIndexReq.getAssetId(), topoPortIndexReq.getPortName());
        List<StatisticsInfoVo> errorCodeIns = hourInterfacesService.errorCodeIns(topoPortIndexReq.getAssetId(),
                topoPortIndexReq.getPortName());
        List<StatisticsInfoVo> errorCodeOuts = hourInterfacesService.errorCodeOuts(topoPortIndexReq.getAssetId(),
                topoPortIndexReq.getPortName());
        topoPortInfoVo.setPortIns(portIns);
        topoPortInfoVo.setPortOuts(portOuts);
        topoPortInfoVo.setDiscardPackageIns(discardPackageIns);
        topoPortInfoVo.setDiscardPackageOuts(discardPackageOuts);
        topoPortInfoVo.setErrorCodeIns(errorCodeIns);
        topoPortInfoVo.setErrorCodeOuts(errorCodeOuts);
        return ResultVoUtil.success(topoPortInfoVo);
    }

    @SuppressWarnings("rawtypes")
    @PostMapping("/portInfo/base")
    @ApiOperation(value = "获取端口基础信息")
    @ResponseBody
    public ResultVo portInfoBase(@RequestBody TopoPortIndexReq topoPortIndexReq) {
        if (StrUtil.isEmpty(topoPortIndexReq.getPortName())) {
            return ResultVoUtil.success("");  //为了前端渲染 直接返回成功
        }
        Asset asset = assetService.getById(topoPortIndexReq.getAssetId());
        if (asset.getAssetMode() == 183) {
            CollectNetworkCard networkCard = networkCardService.findByNetworkName(topoPortIndexReq.getPortName(), topoPortIndexReq.getAssetId());
            TopoPortInfoVo topoPortInfoVo = new TopoPortInfoVo();
            topoPortInfoVo.setPortName(topoPortIndexReq.getPortName());
            topoPortInfoVo.setPhyAddress(topoPortIndexReq.getPortName());
            topoPortInfoVo.setStatus(1);
            topoPortInfoVo.setPortIndexRank(1);
            if (Objects.nonNull(networkCard)) {
                topoPortInfoVo.setStatus(networkCard.getStatus().intValue());
                topoPortInfoVo.setPhyAddress(networkCard.getMacAddress());
            }
            return ResultVoUtil.success(topoPortInfoVo);
        } else {
            TopoPortInfoVo topoPortInfoVo = topoAssetPortService.selectPortIndex(topoPortIndexReq.getAssetId(),
                    topoPortIndexReq.getPortName());
            if (Objects.isNull(topoPortInfoVo)) {
                return ResultVoUtil.error("未找到端口信息");
            }

            // 光交换机端口信息
            String key = RedisCacheConst.OPTICAL_SWITCH_MSG + topoPortIndexReq.getAssetId();
            if (Objects.nonNull(redisService.get(key))) {
                OpticalSwitchBean o = JSONUtil.toBean(redisService.get(key).toString(), OpticalSwitchBean.class);
                if (Objects.nonNull(o.getModuleStateMap())) {
                    String moduleState = o.getModuleStateMap().get(topoPortInfoVo.getPortName());
                    topoPortInfoVo.setModuleState(DisposeOpticalAdapterImpl.moduleStateMap.get(moduleState));
                }
            }
            return ResultVoUtil.success(topoPortInfoVo);
        }
    }

    @SuppressWarnings("rawtypes")
    @PostMapping("/portInfo/inAndOut")
    @ApiOperation(value = "获取端口流入速率和流出速率")
    @ResponseBody
    public ResultVo inAndOut(@RequestBody TopoPortIndexReq topoPortIndexReq) {
        TopoPortInfoVo topoPortInfoVo = new TopoPortInfoVo();

        if (StrUtil.isEmpty(topoPortIndexReq.getPortName())) {
            return ResultVoUtil.success(topoPortInfoVo);
        }

        List<StatisticsInfoVo> portIns = interfaceServ.selectLinePortIn(topoPortIndexReq.getAssetId(),
                topoPortIndexReq.getPortName());
        List<StatisticsInfoVo> portOuts = interfaceServ.selectLinePortOut(topoPortIndexReq.getAssetId(),
                topoPortIndexReq.getPortName());

        Collections.reverse(portIns);
        Collections.reverse(portOuts);
        topoPortInfoVo.setPortIns(portIns);
        topoPortInfoVo.setPortOuts(portOuts);

        return ResultVoUtil.success(topoPortInfoVo);
    }


    @PostMapping("/portInfo/dbm")
    @ApiOperation(value = "获取端口光功率")
    @ResponseBody
    public ResultVo dbm(@RequestBody TopoPortIndexReq topoPortIndexReq) {
        TopoPortInfoVo topoPortInfoVo = new TopoPortInfoVo();

        if (StrUtil.isEmpty(topoPortIndexReq.getPortName())) {
            return ResultVoUtil.success(topoPortInfoVo);
        }

        List<StatisticsInfoVo> dbmIns = interfaceServ.selectLinePortDbmIn(topoPortIndexReq.getAssetId(),
                topoPortIndexReq.getPortName());

        List<StatisticsInfoVo> dbmOuts = interfaceServ.selectLinePortDbmOut(topoPortIndexReq.getAssetId(),
                topoPortIndexReq.getPortName());
        Collections.reverse(dbmIns);
        Collections.reverse(dbmOuts);
        topoPortInfoVo.setDbmIn(dbmIns);
        topoPortInfoVo.setDbmOut(dbmOuts);

        return ResultVoUtil.success(topoPortInfoVo);
    }


    @SuppressWarnings("rawtypes")
    @PostMapping("/portInfo/discard")
    @ApiOperation(value = "获取端口丢包率")
    @ResponseBody
    public ResultVo discard(@RequestBody TopoPortIndexReq topoPortIndexReq) {
        TopoPortInfoVo topoPortInfoVo = new TopoPortInfoVo();

        if (StrUtil.isEmpty(topoPortIndexReq.getPortName())) {
            return ResultVoUtil.success(topoPortInfoVo);
        }

        List<StatisticsInfoVo> discardPackageIns = hourInterfacesService
                .discardPackageIns(topoPortIndexReq.getAssetId(), topoPortIndexReq.getPortName());
        List<StatisticsInfoVo> discardPackageOuts = hourInterfacesService
                .discardPackageOuts(topoPortIndexReq.getAssetId(), topoPortIndexReq.getPortName());

        Collections.reverse(discardPackageIns);
        Collections.reverse(discardPackageOuts);
        topoPortInfoVo.setDiscardPackageIns(discardPackageIns);
        topoPortInfoVo.setDiscardPackageOuts(discardPackageOuts);

        return ResultVoUtil.success(topoPortInfoVo);
    }

    @SuppressWarnings("rawtypes")
    @PostMapping("/portInfo/errorCode")
    @ApiOperation(value = "获取端口误码率")
    @ResponseBody
    public ResultVo errorCode(@RequestBody TopoPortIndexReq topoPortIndexReq) {
        TopoPortInfoVo topoPortInfoVo = new TopoPortInfoVo();

        if (StrUtil.isEmpty(topoPortIndexReq.getPortName())) {
            return ResultVoUtil.success(topoPortInfoVo);
        }

        List<StatisticsInfoVo> errorCodeIns = hourInterfacesService.errorCodeIns(topoPortIndexReq.getAssetId(),
                topoPortIndexReq.getPortName());
        List<StatisticsInfoVo> errorCodeOuts = hourInterfacesService.errorCodeOuts(topoPortIndexReq.getAssetId(),
                topoPortIndexReq.getPortName());
        Collections.reverse(errorCodeIns);
        Collections.reverse(errorCodeOuts);
        topoPortInfoVo.setErrorCodeIns(errorCodeIns);
        topoPortInfoVo.setErrorCodeOuts(errorCodeOuts);

        return ResultVoUtil.success(topoPortInfoVo);
    }

    @GetMapping("/listSettingPort")
    @ApiOperation(value = "获取设置端口")
    @ResponseBody
    public ResultVo listSettingPort(String assetId) {
        List<AssetPortVo> ports = topoAssetPortService.selectAssetAllPort(assetId);
        try {
            List<AssetHidConf> hideList = hidConfService.getFlagListByAsset(assetId, AssetHidConf.TypeEnum.PORT.name());
            List<String> hidNameList = hideList.stream().map(item -> item.getFlag()).collect(Collectors.toList());
            for (AssetPortVo portVo : ports) {
                if (hidNameList.contains(portVo.getPortName())) {
                    portVo.setShow(false);
                } else {
                    portVo.setShow(true);
                }
            }
            return ResultVoUtil.success(ports);
        } catch (Exception e) {
            return ResultVoUtil.success(ports);
        }
    }
}
