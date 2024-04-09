package com.jcca.web.asset.detail;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.jcca.common.bean.ResultVo;
import com.jcca.common.bean.constant.AssetModeConst;
import com.jcca.common.bean.constant.RedisCacheConst;
import com.jcca.common.redis.service.RedisService;
import com.jcca.common.utils.ResultVoUtil;
import com.jcca.component.thresholds.bean.OpticalSwitchBean;
import com.jcca.web.asset.detail.bean.DetailPcb;
import com.jcca.web.asset.detail.bean.DetailVlan;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.asset.service.AssetService;
import com.jcca.web.asset.vo.AssetBelong;
import com.jcca.web.collect.entity.CollectPcb;
import com.jcca.web.collect.entity.CollectSensor;
import com.jcca.web.collect.entity.CollectSystemTime;
import com.jcca.web.collect.entity.CollectVlan;
import com.jcca.web.collect.enums.SensorStatusEnum;
import com.jcca.web.collect.enums.SensorTypeEnum;
import com.jcca.web.collect.service.CollectPcbService;
import com.jcca.web.collect.service.CollectSensorService;
import com.jcca.web.collect.service.CollectSystemTimeService;
import com.jcca.web.collect.service.CollectVlanService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @ClassName RouterDetailService
 * @Description 路由器详情处理器
 * @Date 2020/6/22 13:50
 * @Author hanwone
 */
@Slf4j
@Service
public class RouterDetailService implements DetailAdapter {

    @Resource
    private AssetService assetService;
    @Resource
    private CommonService commonService;
    @Resource
    private CollectVlanService collectVlanService;
    @Resource
    private CollectPcbService collectPcbService;
    @Resource
    private RedisService redisService;
    @Autowired
    private CollectSensorService sensorServ;
    @Resource
    private CollectSystemTimeService sysTimeServ;

    @Override
    public String getCode() {
        return String.valueOf(AssetModeConst.ROUTER);
    }

    @Override
    public ResultVo handle(Asset asset) {
        String assetId = asset.getId();
        AssetBelong assetBelong = assetService.findAssetBelongById(assetId);
        if (Objects.isNull(assetBelong)) {
            return ResultVoUtil.error("资产[" + assetId + "]附属信息不存在");
        }

        // 端口详情 TODO

        // VLAN信息
        List<DetailVlan> vlanList = new ArrayList<>();
        List<CollectVlan> vlans = collectVlanService.getRealTimeData(assetId);
        for (CollectVlan vlan : vlans) {
            DetailVlan v = new DetailVlan();
            BeanUtil.copyProperties(vlan, v);
            vlanList.add(v);
        }

        // 板卡信息
        List<DetailPcb> pcbList = new ArrayList<>();
        List<CollectPcb> pcbs = collectPcbService.getRealTimeData(assetId);
        for (CollectPcb pcb : pcbs) {
            DetailPcb p = new DetailPcb();
            BeanUtil.copyProperties(pcb, p);
            pcbList.add(p);
        }

        // 返回结果
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("isOptical", false);
        // 光交换机相关信息
        this.getOpticalInfo(resultMap, assetId);

        // 获取通用信息
        resultMap.put("generalInfo", commonService.getDetailGeneral(assetBelong, asset));
        // 获取CPU信息
        resultMap.put("cpuInfo", commonService.getDetailCPU(asset, assetId));
        // 内存信息
        resultMap.put("memorySwapInfo", commonService.getDetailMemorySwap(assetId));
        // CPU折线图
        resultMap.put("cpuLine", commonService.getLineCPU(assetId));
        // 内存折线图
        resultMap.put("memoryLine", commonService.getMemLine(assetId));
        resultMap.put("interfacesInfo", "interfacesInfo");
        resultMap.put("vlanInfo", vlanList);
        resultMap.put("pcbInfo", pcbList);
        resultMap.put("assetImage", asset.getAssetImage());

        // 风扇、温度、电源信息
        List<CollectSensor> sensotList = sensorServ.getRealTimeData(asset.getId());
        List<CollectSensor> fanList = sensotList.stream()
                .filter(item -> SensorTypeEnum.FAN.name().equals(item.getSensorType())).collect(Collectors.toList());
        List<CollectSensor> gaugeListTemp = sensotList.stream()
                .filter(item -> SensorTypeEnum.GAUGE.name().equals(item.getSensorType())).collect(Collectors.toList());

        List<CollectSensor> gaugeList = new ArrayList<CollectSensor>();
        for (CollectSensor collectSensor : gaugeListTemp) {
            String value = collectSensor.getValue();
            if (StrUtil.isEmpty(value)) {
                continue;
            }
            try {
                if (NumberUtil.isDouble(value) || NumberUtil.isNumber(value)) {
                    if (Double.parseDouble(value) != 0) {
                        gaugeList.add(collectSensor);
                    }
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }

        List<CollectSensor> powerList = sensotList.stream()
                .filter(item -> SensorTypeEnum.POWER.name().equals(item.getSensorType())).collect(Collectors.toList());

        resultMap.put("powerList", powerList);
        resultMap.put("gaugeList", gaugeList);
        resultMap.put("fanList", fanList);

        resultMap.put("powerStatus", getStatus(powerList));
        resultMap.put("fanStatus", getStatus(fanList));
        resultMap.put("gaugeStatus", getStatus(gaugeList));

        //分析最后一次采集时间
        Date date = assetService.queryNetLastTimeDate(assetId);
        if (Objects.nonNull(date)) {
            resultMap.put("lastDate", DateUtil.format(date, "yyyy-MM-dd HH:mm:ss"));
        } else {
            resultMap.put("lastDate", "----");
        }
        List<CollectSystemTime> timeList = sysTimeServ.getRealTimeData(assetId);
        if(Objects.nonNull(timeList) && !timeList.isEmpty()){
            Long timeDuration = timeList.get(0).getTimeduration();
            if(Objects.nonNull(timeDuration)){
                resultMap.put("timeduration",timeDuration);
            }
        }

        return ResultVoUtil.success(resultMap);
    }

    private void getOpticalInfo(Map<String, Object> resultMap, String assetId) {
        Object o = redisService.get(RedisCacheConst.OPTICAL_SWITCH_MSG + assetId);
        if (Objects.nonNull(o)) {
            resultMap.put("isOptical", true);
            OpticalSwitchBean optical = JSONUtil.toBean(o.toString(), OpticalSwitchBean.class);
            Map<String, String> pwrStateMap = optical.getPwrStateMap();
            Map<String, String> fanStateMap = optical.getFanStateMap();
            Map<String, String> opticalHealthMap = optical.getOpticalHealthMap();

            if (Objects.isNull(pwrStateMap)) {
                pwrStateMap = new HashMap<>();
            }
            if (Objects.isNull(fanStateMap)) {
                fanStateMap = new HashMap<>();
            }
            if (Objects.isNull(opticalHealthMap)) {
                opticalHealthMap = new HashMap<>();
            }

            List<Map<String, Object>> powerList = new ArrayList<>();
            List<Map<String, Object>> fanList = new ArrayList<>();
            List<Map<String, Object>> healthList = new ArrayList<>();
            Set<Map.Entry<String, String>> entries = pwrStateMap.entrySet();
            int i = 1;
            for (Map.Entry<String, String> entry : entries) {
                String value = entry.getValue();
                Map<String, Object> map = new HashMap<>();
                map.put("state", value.contains("OK"));
                map.put("name", "电源 #" + (i++));
                powerList.add(map);
            }

            Set<Map.Entry<String, String>> entries1 = fanStateMap.entrySet();
            for (Map.Entry<String, String> entry : entries1) {
                String value = entry.getValue();
                Map<String, Object> map = new HashMap<>();
                map.put("state", value.contains("Ok"));
                map.put("speed", value.substring(value.length() - 8));
                map.put("name", value.substring(0, 5));
                fanList.add(map);
            }

            Set<Map.Entry<String, String>> entries2 = opticalHealthMap.entrySet();
            for (Map.Entry<String, String> entry : entries2) {
                String value = entry.getValue().replace("\t", " ");
                Map<String, Object> map = new HashMap<>();

                if (value.contains("Temperatures")) {
                    map.put("state", value.contains("HEALTHY"));
                    map.put("name", "温度传感器");
                    healthList.add(map);
                }
                if (value.contains("Fans")) {
                    map.put("state", value.contains("HEALTHY"));
                    map.put("name", "风扇传感器");
                    healthList.add(map);
                }
                if (value.contains("Power")) {
                    map.put("state", value.contains("HEALTHY"));
                    map.put("name", "电源传感器");
                    healthList.add(map);
                }
                if (value.contains("SwitchState")) {
                    map.put("state", value.contains("HEALTHY"));
                    map.put("name", "交换机状态");
                    healthList.add(map);
                }
            }

            resultMap.put("powerModuleList", powerList);
            resultMap.put("fanModuleList", fanList);
            resultMap.put("healthList", healthList);
        }
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
