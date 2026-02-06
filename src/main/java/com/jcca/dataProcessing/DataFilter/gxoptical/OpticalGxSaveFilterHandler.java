package com.jcca.dataProcessing.DataFilter.gxoptical;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.jcca.common.utils.MyIdUtil;
import com.jcca.component.thresholds.bean.OpticalSwitchV2Bean;
import com.jcca.component.thresholds.bean.TSensor;
import com.jcca.dataProcessing.support.IFilterHandler;
import com.jcca.web.collect.entity.CollectSensor;
import com.jcca.web.collect.enums.SensorTypeEnum;
import com.jcca.web.collect.service.CollectSensorService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 光纤交换机信息保存处理类
 */
@Component("opticalGxSaveFilterHandler")
public class OpticalGxSaveFilterHandler extends IFilterHandler<OpticalSwitchV2Bean> {

    @Resource
    private CollectSensorService collectSensorService;

    @Override
    public boolean handler(OpticalSwitchV2Bean bean) {

        String collectCode = MyIdUtil.getId();

        String assetId = bean.getAssetId();
        if (StrUtil.isEmpty(assetId)) {
            return false;
        }

        Date collectTime = new Date(bean.getCollectTime()); // 直接构造 Date

        List<CollectSensor> sensorList = new ArrayList<>();

        // 处理温度传感器（对应 "Ok"）
        handleTemperatureSensors(bean, assetId, collectCode, collectTime, sensorList);

        // 处理风扇传感器（也属于 "Ok" 状态）
        handleFanSensors(bean, assetId, collectCode, collectTime, sensorList);

        // 处理电源传感器（对应 "OK"）
        handlePowerSensors(bean, assetId, collectCode, collectTime, sensorList);

        // 光模块健康状态（对应 "HEALTHY"，按需扩展）
        handleOpticalHealthSensors(bean, assetId, collectCode, collectTime, sensorList);

        if (CollectionUtil.isNotEmpty(sensorList)) {
            collectSensorService.saveBatch(sensorList);
        }

        return true;
    }

    @Override
    public boolean isNeedNextHandle(Boolean flag) {
        return flag;
    }


    // ------------------ 温度传感器处理 ------------------
    private void handleTemperatureSensors(OpticalSwitchV2Bean bean, String assetId, String collectCode, Date collectTime, List<CollectSensor> sensorList) {
        List<TSensor> temperatureMap = bean.getTemperatureMap();
        if (CollectionUtil.isEmpty(temperatureMap)) return;

        for (TSensor ts : temperatureMap) {
            CollectSensor sensor = new CollectSensor();
            sensor.setAssetId(assetId);
            sensor.setCollectCode(collectCode);
            sensor.setCollectTime(collectTime);
            sensor.setCreateTime(new Date());

            String value = ts.getValue();
            String serialName = ts.getSerialNumberName(); // 假设字段名如此

            sensor.setValue(value);
            sensor.setStatus(String.valueOf(ts.getStatus()));
            sensor.setSensorType(SensorTypeEnum.GAUGE.name());
            sensor.setDescStr("Temperatures monitor HEALTHY");
            sensor.setSerialNumberName(serialName);

            sensorList.add(sensor);
        }
    }

    // ------------------ 风扇传感器处理 ------------------
    private void handleFanSensors(OpticalSwitchV2Bean bean, String assetId, String collectCode, Date collectTime, List<CollectSensor> sensorList) {
        List<TSensor> fanStateMap = bean.getFanStateMap();
        if (CollectionUtil.isEmpty(fanStateMap)) return;

        for (TSensor ts : fanStateMap) {
            CollectSensor sensor = buildBaseSensor(assetId, collectCode, collectTime, ts);
            sensor.setSensorType(SensorTypeEnum.FAN.name());
            sensor.setDescStr("Fan state: " + ts.getValue());
            sensorList.add(sensor);
        }
    }

    // ------------------ 电源传感器处理 ------------------
    private void handlePowerSensors(OpticalSwitchV2Bean bean, String assetId, String collectCode, Date collectTime, List<CollectSensor> sensorList) {
        List<TSensor> pwrStateMap = bean.getPwrStateMap();
        if (CollectionUtil.isEmpty(pwrStateMap)) return;

        for (TSensor ts : pwrStateMap) {
            CollectSensor sensor = buildBaseSensor(assetId, collectCode, collectTime, ts);
            sensor.setSensorType(SensorTypeEnum.POWER.name());
            sensor.setDescStr("Power supplies monitor HEALTHY");
            sensorList.add(sensor);
        }
    }

    // ------------------ 健康状态 ------------------
    private void handleOpticalHealthSensors(OpticalSwitchV2Bean bean, String assetId, String collectCode, Date collectTime, List<CollectSensor> sensorList) {
        List<TSensor> opticalHealthMap = bean.getOpticalHealthMap();
        if (CollectionUtil.isEmpty(opticalHealthMap)) return;

        for (TSensor ts : opticalHealthMap) {
            CollectSensor sensor = buildBaseSensor(assetId, collectCode, collectTime, ts);
            sensor.setStatus(String.valueOf(ts.getStatus()));
            sensor.setDescStr("Optical module health status");
            sensorList.add(sensor);
        }
    }

    // ------------------ 构建基础传感器对象 ------------------
    private CollectSensor buildBaseSensor(String assetId, String collectCode, Date collectTime, TSensor ts) {
        CollectSensor sensor = new CollectSensor();
        sensor.setAssetId(assetId);
        sensor.setCollectCode(collectCode);
        sensor.setCollectTime(collectTime);
        sensor.setCreateTime(new Date());

        String value = ts.getValue();
        sensor.setValue(value);
        sensor.setStatus(String.valueOf(ts.getStatus()));
        sensor.setDescStr(ts.getDescStr());
        sensor.setSerialNumberName(ts.getSerialNumberName());
        return sensor;
    }

}
