package com.jcca.component.thresholds.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.jcca.common.utils.EntityBeanUtil;
import com.jcca.common.utils.MyIdUtil;
import com.jcca.component.constants.ReceiveCollectConst;
import com.jcca.component.event.EventLogicService;
import com.jcca.component.event.bean.CreateEventReq;
import com.jcca.component.event.constant.EventGroupConstant;
import com.jcca.component.event.constant.EventUniqueCode;
import com.jcca.component.thresholds.CollectAdapter;
import com.jcca.component.thresholds.bean.CollectSensorBean;
import com.jcca.web.asset.service.ThresholdAssetService;
import com.jcca.web.asset.service.bean.VerifyThresholdSectionResp;
import com.jcca.web.collect.entity.CollectSensor;
import com.jcca.web.collect.enums.SensorTypeEnum;
import com.jcca.web.collect.service.CollectSensorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 环境传感数据处理
 *
 * @author Lvyp
 */
@Slf4j
@Component
public class DisposeSensorAdpaterImpl implements CollectAdapter {

    @Resource
    private CollectSensorService sensorService;
    @Resource
    private ThresholdAssetService thresholdServ;
    @Resource
    private EventLogicService eventLogicServ;

    /**
     * 处理环境传感采集数据
     *
     * @param data
     */

    @Override
    public void dispose(JSONArray data) {
        List<CollectSensorBean> sensorList = JSONUtil.toList(data, CollectSensorBean.class);
        List<CollectSensor> sensors = new ArrayList<CollectSensor>();
        String collectCode = MyIdUtil.getId();
        for (CollectSensorBean item : sensorList) {
            if (StrUtil.isEmpty(item.getAssetId()) || StrUtil.isEmpty(item.getCollectTime())
                    || StrUtil.isEmpty(item.getSensorType()) || StrUtil.isEmpty(item.getValue())) {
                continue;
            }

            Date date = new Date();
            date.setTime(Long.parseLong(item.getCollectTime()));

            if (SensorTypeEnum.GAUGE.name().equals(item.getSensorType())) {
                //温度类型 阶段阈值
                VerifyThresholdSectionResp thresholdSectionResp = thresholdServ.verifySectionThreshold(Double.valueOf(item.getValue()), EventUniqueCode.COLLECTOR_SENSOR_GAUGE);
                List<CreateEventReq> eventList = thresholdServ.disposeVerifyThresholdSectionResp(thresholdSectionResp, EventGroupConstant.SENSOR_GAUGE, date, item.getAssetId(), item.getValue(), "温度传感："+item.getDescStr());

                try {
                    for (CreateEventReq createEventReq : eventList) {
                        eventLogicServ.addEvent(createEventReq);
                    }
                } catch (Exception e) {
                    log.error("处理温度事件异常：{}", e.getMessage(), e);
                }
            }

            CollectSensor sensor = EntityBeanUtil.copy(item, CollectSensor.class);
            sensor.setCollectCode(collectCode);
            sensor.setCollectTime(date);
            sensor.setId(MyIdUtil.getId());
            sensors.add(sensor);
        }

        if (sensors.isEmpty()) {
            return;
        }
        // 刷新缓存
        sensorService.updateRealTimeData(sensors);
        // 更新数据
        sensorService.saveBatch(sensors);

    }

    @Override
    public String getCode() {

        return ReceiveCollectConst.SENSOR;
    }

}
