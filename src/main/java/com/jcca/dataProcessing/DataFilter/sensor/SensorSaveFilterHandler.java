package com.jcca.dataProcessing.DataFilter.sensor;

import com.jcca.common.utils.EntityBeanUtil;
import com.jcca.common.utils.MyIdUtil;
import com.jcca.dataProcessing.Entity.CollectSensorEntity;
import com.jcca.dataProcessing.support.IFilterHandler;
import com.jcca.web.collect.entity.CollectSensor;
import com.jcca.web.collect.service.CollectSensorService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;

/**
 * @author Zhaozheng
 * @description TODO
 * @className sensorStateFilterHandler
 * @date 2023/10/27 9:59
 * @since 2.1.0.0
 */
@Component("sensorSaveFilterHandler")
public class SensorSaveFilterHandler extends IFilterHandler<CollectSensorEntity> {

    @Resource
    private CollectSensorService sensorService;

    @Override
    public boolean handler(CollectSensorEntity info) {
        Date date = new Date();
        if (info.getCollectTime() == null) {
            date.setTime(new Date().getTime());
        } else {
            date.setTime(info.getCollectTime());
        }

        CollectSensor sensor = EntityBeanUtil.copy(info, CollectSensor.class);
        sensor.setCollectCode(info.getCollectCode());
        sensor.setCollectTime(date);
        sensor.setId(MyIdUtil.getId());

        // 更新数据
        sensorService.save(sensor);
        return true;
    }

    @Override
    public boolean isNeedNextHandle(Boolean flag) {
        return flag;
    }


}
