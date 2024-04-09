package com.jcca.dataProcessing.DataFilter.optical;

import cn.hutool.core.collection.CollectionUtil;
import com.jcca.dataProcessing.Entity.OpticalSwitchEntity;
import com.jcca.dataProcessing.support.IFilterHandler;
import com.jcca.web.collect.entity.CollectSensor;
import com.jcca.web.collect.enums.SensorTypeEnum;
import com.jcca.web.collect.service.CollectSensorService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;

/**
 * @author Zhaozheng
 * @description TODO 光纤交换机信息保存处理类
 * @className OpticalFanFilterHandler
 * @date 2023/10/27 9:48
 * @since 2.1.0.0
 */
@Component("opticalSaveFilterHandler")
public class OpticalSaveFilterHandler extends IFilterHandler<OpticalSwitchEntity> {

    @Resource
    private CollectSensorService collectSensorService;

    @Override
    public boolean handler(OpticalSwitchEntity bean) {

        List<CollectSensor> sensorList = new ArrayList<CollectSensor>();
        Map<String, String> fanStateMap = bean.getFanStateMap();
        Map<String, String> pwrStateMap = bean.getPwrStateMap();
        Map<String, String> opticalHealthMap = bean.getOpticalHealthMap();
        if (Objects.nonNull(fanStateMap)) {
            Set<Map.Entry<String, String>> entries = fanStateMap.entrySet();
            for (Map.Entry<String, String> entry : entries) {
                getSensor(bean.getCollectCode(), bean, bean.getAssetId(), sensorList, entry, "Ok");
            }
        }
        if (Objects.nonNull(pwrStateMap)) {
            Set<Map.Entry<String, String>> entries1 = pwrStateMap.entrySet();
            for (Map.Entry<String, String> entry : entries1) {
                getSensor(bean.getCollectCode(), bean, bean.getAssetId(), sensorList, entry, "OK");
            }
        }
        if (Objects.nonNull(opticalHealthMap)) {
            Set<Map.Entry<String, String>> entries2 = opticalHealthMap.entrySet();
            for (Map.Entry<String, String> entry : entries2) {
                getSensor(bean.getCollectCode(), bean, bean.getAssetId(), sensorList, entry, "HEALTHY");
            }
        }


        if (CollectionUtil.isNotEmpty(sensorList)) {
            collectSensorService.saveBatch(sensorList);
        }

        return true;
    }

    @Override
    public boolean isNeedNexthandle(Boolean flag) {
        return flag;
    }


    private void getSensor(String collectCode, OpticalSwitchEntity bean, String assetId, List<CollectSensor> sensorList,
                           Map.Entry<String, String> entry, String healthy) {
        Date date = new Date();
        date.setTime(bean.getCollectTime());

        CollectSensor sensor = new CollectSensor();
        sensor.setAssetId(assetId);
        sensor.setCollectCode(collectCode);
        sensor.setCollectTime(date);
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
            sensor.setSerialNumberName("温度传感" + entry.getKey());
            sensor.setSensorType(SensorTypeEnum.GAUGE.name());
            sensor.setDescStr("Temperatures monitor HEALTHY");


            // 处理风扇
            CollectSensor s = new CollectSensor();
            s.setAssetId(assetId);
            s.setCollectCode(collectCode);
            s.setCollectTime(date);
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

}
