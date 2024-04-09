package com.jcca.web.asset.detail;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jcca.admin.system.entity.SysOrg;
import com.jcca.admin.system.service.SysOrgService;
import com.jcca.common.bean.ResultVo;
import com.jcca.common.bean.constant.AssetModeConst;
import com.jcca.common.bean.constant.RedisCacheConst;
import com.jcca.common.enums.UnitEnum;
import com.jcca.common.redis.service.RedisService;
import com.jcca.common.utils.ResultVoUtil;
import com.jcca.component.thresholds.bean.CollectProcessBean;
import com.jcca.dataProcessing.enums.StatusInfoChangeTypeEnum;
import com.jcca.web.asset.detail.bean.*;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.asset.entity.CollectAIXAdapter;
import com.jcca.web.asset.entity.ThresholdProcess;
import com.jcca.web.asset.service.AssetService;
import com.jcca.web.asset.service.CollectAixAdapterService;
import com.jcca.web.asset.service.ThresholdProcessService;
import com.jcca.web.asset.vo.AssetBelong;
import com.jcca.web.collect.controller.route.bean.RouteMsg;
import com.jcca.web.collect.entity.CollectCluster;
import com.jcca.web.collect.entity.CollectNetworkCard;
import com.jcca.web.collect.entity.CollectSensor;
import com.jcca.web.collect.entity.CollectSystemTime;
import com.jcca.web.collect.enums.SensorStatusEnum;
import com.jcca.web.collect.enums.SensorTypeEnum;
import com.jcca.web.collect.service.*;
import com.jcca.web.collect.service.bean.AssetDiskVo;
import com.jcca.web.collect.service.bean.DiskVo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @ClassName ServerDetailService
 * @Description 服务器详情处理器
 * @Date 2020/6/22 13:46
 * @Author hanwone
 */
@Service
public class ServerDetailService implements DetailAdapter {
    @Resource
    private CommonService commonService;
    @Resource
    private AssetService assetService;
    @Resource
    private CollectDiskService collectDiskService;
    @Resource
    private CollectNetworkCardService collectNetworkCardService;
    @Resource
    private ThresholdProcessService thresholdProcessServ;
    @Resource
    private CollectSensorService sensorServ;
    @Resource
    private CollectRouteService routeServ;
    @Resource
    private RedisService redisService;
    @Resource
    private CollectAixAdapterService aixAdapterService;
    @Resource
    private CollectSystemTimeService sysTimeServ;
    @Resource
    private SysOrgService orgServ;
    @Resource
    private CollectClusterService collectClusterServ;

    @Override
    public String getCode() {
        return String.valueOf(AssetModeConst.SERVER);
    }

    @Override
    public ResultVo handle(Asset asset) {
        String assetId = asset.getId();
        AssetBelong assetBelong = assetService.findAssetBelongById(assetId);
        if (Objects.isNull(assetBelong)) {
            return ResultVoUtil.error("资产[" + assetId + "]附属信息不存在");
        }

        DetailGeneral detailGeneral = commonService.getDetailGeneral(assetBelong, asset);
        // 磁盘信息
        AssetDiskVo assetDiskVo = collectDiskService.getAssetDiskMsg(assetId, UnitEnum.AUTO);

        List<DetailDisk> diskList = new ArrayList<>();
        if (Objects.nonNull(assetDiskVo)) {
            detailGeneral.setDiskCapacity(assetDiskVo.getDiskTotalStr());

            List<DiskVo> diskVoList = assetDiskVo.getDiskList();
            for (DiskVo diskVo : diskVoList) {
                DetailDisk disk = new DetailDisk();
                BeanUtil.copyProperties(diskVo, disk);
                diskList.add(disk);
            }
        }

        // 网卡信息
        List<DetailNetWorkCard> cardList = new ArrayList<>();
        List<CollectNetworkCard> networkCardList = collectNetworkCardService.getRealTimeData(assetId);
        for (CollectNetworkCard card : networkCardList) {
            if (StrUtil.isEmpty(card.getIp())) {
                continue;
            }
            RouteMsg routeMsg = routeServ.queryRouteMsg(card.getAssetId(), card.getMacAddress());

            DetailNetWorkCard c = new DetailNetWorkCard();
            c.setName(card.getName());
            c.setMacAddress(card.getMacAddress());
            c.setIp(card.getIp());
            c.setCollectTime(card.getCollectTime());
            c.setStatus(card.getStatus());
            c.setRouteMsg(routeMsg);
            c.setCollectTime(card.getCollectTime());
            cardList.add(c);
        }

        // 进程信息
        List<DetailProcess> processList = new ArrayList<>();
        QueryWrapper<ThresholdProcess> queryWrapper = new QueryWrapper<ThresholdProcess>();
        queryWrapper.eq("ASSET_ID", asset.getId());
        List<ThresholdProcess> processBeanListList = thresholdProcessServ.list(queryWrapper);
        for (ThresholdProcess processBean : processBeanListList) {
            DetailProcess process = new DetailProcess();
            process.setName(processBean.getProcessName());
            process.setCpuUsedRate(StrUtil.isEmpty(processBean.getCpuRate()) ? "--" : processBean.getCpuRate());
            process.setMemUsedRate(StrUtil.isEmpty(processBean.getMemoryRate()) ? "--" : processBean.getMemoryRate());
            process.setStatus(processBean.getCollectStatus());
            process.setRemark(processBean.getRemark());
            processList.add(process);
        }

        //集群信息
        QueryWrapper<CollectCluster> clusterQuery = new QueryWrapper<>();
        clusterQuery.eq("ASSET_ID",assetId);
        clusterQuery.orderByDesc("SERVER_ROLE");
        List<CollectCluster> list = collectClusterServ.list(clusterQuery);

        //内存
        List<AssetLine> memLine = commonService.getMemLine(assetId);
        // 返回结果
        Map<String, Object> resultMap = new HashMap<>();

        resultMap.put("abFlag",asset.getABFlag());
        resultMap.put("clusterList", list);
        resultMap.put("diskInfo", diskList);
        resultMap.put("netCardInfo", cardList);
        resultMap.put("processInfo", processList);
        // 获取通用信息
        resultMap.put("generalInfo", detailGeneral);
        // 内存信息
        resultMap.put("memorySwapInfo", commonService.getDetailMemorySwap(assetId));
        // 获取CPU信息
        resultMap.put("cpuInfo", commonService.getDetailCPU(asset, assetId));
        // CPU折线图
        List<AssetLine> lineCPU = commonService.getLineCPU(assetId);
        resultMap.put("cpuLine", lineCPU);
        // 内存折线图
        resultMap.put("memoryLine", memLine);

        // 风扇、温度、电源信息
        List<CollectSensor> sensotList = sensorServ.getRealTimeData(assetId);
        List<CollectSensor> fanList = sensotList.stream()
                .filter(item -> SensorTypeEnum.FAN.name().equals(item.getSensorType())).collect(Collectors.toList());
        List<CollectSensor> gaugeListTemp = sensotList.stream()
                .filter(item -> SensorTypeEnum.GAUGE.name().equals(item.getSensorType())).collect(Collectors.toList());

        List<CollectSensor> gaugeList = new ArrayList<CollectSensor>();
        for (CollectSensor collectSensor : gaugeListTemp) {
            String value = collectSensor.getValue();
            if (NumberUtil.isDouble(value) || NumberUtil.isNumber(value)) {
                if (Double.parseDouble(value) != 0) {
                    gaugeList.add(collectSensor);
                }
            }
        }

        List<CollectSensor> powerList = sensotList.stream()
                .filter(item -> SensorTypeEnum.POWER.name().equals(item.getSensorType())).collect(Collectors.toList());

        SysOrg sysOrg = orgServ.getById(asset.getOrgId());

        resultMap.put("org",sysOrg);
        resultMap.put("powerList", powerList);
        resultMap.put("gaugeList", gaugeList);
        resultMap.put("fanList", fanList);

        resultMap.put("powerStatus", getStatus(powerList));
        resultMap.put("fanStatus", getStatus(fanList));
        resultMap.put("gaugeStatus", getStatus(gaugeList));
        resultMap.put("assetImage", asset.getAssetImage());
        resultMap.put("desk",asset.getDesk());
        resultMap.put("collectionType", asset.getCollectionType());
        //分析最后一次采集时间
        Date date = assetService.queryServerLastTimeDate(assetId);
        String lastTime = "";
        if (Objects.nonNull(date)) {
            lastTime = DateUtil.format(date, "yyyy-MM-dd HH:mm:ss");
            resultMap.put("lastDate", lastTime);
        } else {
            resultMap.put("lastDate", "----");
        }

        //Object cpu = redisService.get(RedisCacheConst.TOP5_PROCESS_CPU + assetId);
        //Object mem = redisService.get(RedisCacheConst.TOP5_PROCESS_MEM + assetId);
        List<CollectProcessBean> memTop5V2 = redisService.getProcessTop5V2(asset.getIp(), asset.getId(),lastTime, StatusInfoChangeTypeEnum.status_MEMTop5);
        List<CollectProcessBean> cpuTop5V2 = redisService.getProcessTop5V2(asset.getIp(), asset.getId(), lastTime,StatusInfoChangeTypeEnum.status_CPUTop5);
        if (Objects.nonNull(cpuTop5V2)) {
            //需要转换一下
            resultMap.put("processTop5cpu", cpuTop5V2);
        }
        if (Objects.nonNull(memTop5V2)) {
            resultMap.put("processTop5Mem", memTop5V2);
        }

        // 小机IO卡信息 20230208
        if (Objects.nonNull(asset.getDesk()) && asset.getDesk() == AssetModeConst.SMALL_SERVER.intValue()) {
            List<CollectAIXAdapter> aixAdapterList = aixAdapterService.latestInfo(assetId);
            resultMap.put("aixIoCardList", aixAdapterList);
        }


        List<CollectSystemTime> timeList = sysTimeServ.getRealTimeData(assetId);
        if(Objects.nonNull(timeList) && !timeList.isEmpty()){
            Long timeduration = timeList.get(0).getTimeduration();
            if(Objects.nonNull(timeduration)){
                resultMap.put("timeduration",timeduration);
            }
        }
        return ResultVoUtil.success(resultMap);
    }

    @Override
    public ResultVo getAssetGeneralInfo(Asset asset) {
        return commonService.commonInfo(asset);
    }

    /**
     * 空未知
     *
     * @param sensorList
     * @return
     */
    private Boolean getStatus(List<CollectSensor> sensorList) {
        for (CollectSensor item : sensorList) {
            if (!SensorStatusEnum.NORMAL.getCode().equals(item.getStatus())) {
                return false;
            }
        }
        if (sensorList.isEmpty()) {
            return null;
        }
        return true;
    }

}
