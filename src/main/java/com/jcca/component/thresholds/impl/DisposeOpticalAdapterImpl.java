package com.jcca.component.thresholds.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.jcca.common.utils.MyIdUtil;
import com.jcca.component.constants.ReceiveCollectConst;
import com.jcca.component.event.EventLogicService;
import com.jcca.component.event.bean.CreateEventReq;
import com.jcca.component.event.constant.EventGroupConstant;
import com.jcca.component.event.constant.EventUniqueCode;
import com.jcca.component.thresholds.CollectAdapter;
import com.jcca.component.thresholds.bean.OpticalSwitchBean;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.asset.service.AssetService;
import com.jcca.web.asset.service.ThresholdAssetService;
import com.jcca.web.asset.service.bean.VerifyThresholdSectionResp;
import com.jcca.web.collect.entity.CollectSensor;
import com.jcca.web.collect.enums.SensorTypeEnum;
import com.jcca.web.collect.service.CollectSensorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

/**
 * @Description 处理光交换机采集数据
 * @ClassName DisposeOpticalAdapterImpl
 * @Date 2022/6/15 11:19
 * @Author hanwone
 * @Since 2.0.0.1
 */
@Service
@Slf4j
public class DisposeOpticalAdapterImpl implements CollectAdapter {

    @Resource
    private AssetService assetService;
    @Resource
    private ThresholdAssetService thresholdServ;
    @Resource
    private EventLogicService eventLogicServ;

    @Resource
    private CollectSensorService collectSensorService;

    public static Map<String, String> moduleStateMap = new HashMap<String, String>() {{
        put("2", "没有光模块");
        put("3", "光模块故障");
        put("4", "没有光信号");
        put("5", "光信号不同步");
        put("6", "正常");
        put("7", "端口故障");
        put("8", "板卡故障");
    }};

    /**
     * 处理数据
     *
     * @param data 采集到的数据
     */
    @Override
    public void dispose(JSONArray data) {
        List<OpticalSwitchBean> beanList = JSONUtil.toList(data, OpticalSwitchBean.class);
        if (CollectionUtil.isEmpty(beanList)) {
            log.error("光交换机采集数据处理失败，空的序列集合");
            return;
        }

        String collectCode = MyIdUtil.getId();
        for (OpticalSwitchBean bean : beanList) {
            String assetId = bean.getAssetId();
            if (StrUtil.isEmpty(assetId)) {
                log.error("光交换机采集数据处理失败，缺少资产ID");
                continue;
            }

            Asset asset = assetService.getById(assetId);
            if (Objects.isNull(asset)) {
                log.error("光交换机采集数据处理失败，资产不存在，ID为：{}", assetId);
                continue;
            }

            List<CollectSensor> sensorList = new ArrayList<>();

            Map<String, String> fanStateMap = bean.getFanStateMap();
            if (Objects.nonNull(fanStateMap)) {
                Set<Map.Entry<String, String>> entries = fanStateMap.entrySet();
                for (Map.Entry<String, String> entry : entries) {
                    getSensor(collectCode, bean, assetId, sensorList, entry, "Ok");
                }
            }

            Map<String, String> pwrStateMap = bean.getPwrStateMap();
            if (Objects.nonNull(pwrStateMap)) {
                Set<Map.Entry<String, String>> entries1 = pwrStateMap.entrySet();
                for (Map.Entry<String, String> entry : entries1) {
                    getSensor(collectCode, bean, assetId, sensorList, entry, "OK");
                }
            }

            Map<String, String> opticalHealthMap = bean.getOpticalHealthMap();
            if (Objects.nonNull(opticalHealthMap)) {
                Set<Map.Entry<String, String>> entries2 = opticalHealthMap.entrySet();
                for (Map.Entry<String, String> entry : entries2) {
                    getSensor(collectCode, bean, assetId, sensorList, entry, "HEALTHY");
                }
            }
            if (CollectionUtil.isNotEmpty(sensorList)) {
                collectSensorService.saveBatch(sensorList);
            }
        }
//        log.info("收到光交换机采集信息：" + JSONUtil.toJsonStr(beanList));
    }

    private void getSensor(String collectCode, OpticalSwitchBean bean, String assetId, List<CollectSensor> sensorList,
                           Map.Entry<String, String> entry, String healthy) {
        CollectSensor sensor = new CollectSensor();
        sensor.setAssetId(assetId);
        sensor.setCollectCode(collectCode);
        sensor.setCollectTime(DateUtil.parseDateTime(bean.getCollectTime()));
        String value = entry.getValue();
        sensor.setStatus(value.contains(healthy) ? "1" : "0");
        sensor.setSensorType("");

        sensor.setDescStr(value);
        sensor.setSerialNumberName("");

        if ("Ok".equals(healthy)) {
            // 处理温度
            Map<String, String> temperatureMap = bean.getTemperatureMap();
            String key = temperatureMap.get(entry.getKey());
            sensor.setValue(key);
            sensor.setSerialNumberName("温度传感"+entry.getKey());
            sensor.setSensorType(SensorTypeEnum.GAUGE.name());
            sensor.setDescStr("Temperatures monitor HEALTHY");

            //温度阈值处理
            //温度类型 阶段阈值
            try {
                VerifyThresholdSectionResp thresholdSectionResp = thresholdServ.verifySectionThreshold(Double.valueOf(key), EventUniqueCode.COLLECTOR_SENSOR_GAUGE);
                List<CreateEventReq> eventList = thresholdServ.disposeVerifyThresholdSectionResp(thresholdSectionResp, EventGroupConstant.SENSOR_GAUGE, sensor.getCollectTime(), assetId, key, "Temperatures "+entry.getKey());
                for (CreateEventReq createEventReq : eventList) {
                    eventLogicServ.addEvent(createEventReq);
                }
            } catch (Exception e) {
                log.error("处理温度事件异常：{}", e.getMessage(), e);
            }

            // 处理风扇
            CollectSensor s = new CollectSensor();
            s.setAssetId(assetId);
            s.setCollectCode(collectCode);
            s.setCollectTime(DateUtil.parseDateTime(bean.getCollectTime()));
            String v = entry.getValue();
            s.setStatus(v.contains(healthy) ? "1" : "0");
            s.setDescStr(v);
            s.setSensorType(SensorTypeEnum.FAN.name());
            s.setValue(v.split("\\s+")[6]);
            s.setCreateTime(new Date());
            sensorList.add(s);
        }
        if ("OK".equals(healthy)) {
            sensor.setSensorType(SensorTypeEnum.POWER.name());
            sensor.setDescStr("Power supplies monitor HEALTHY");
            sensor.setSerialNumberName("");
        }

        sensor.setCreateTime(new Date());
        sensorList.add(sensor);
    }

    /**
     * 获取对应的KEY
     *
     * @return
     */
    @Override
    public String getCode() {
        return ReceiveCollectConst.OPTICAL_SWITCH;
    }
}
