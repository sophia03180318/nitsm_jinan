package com.jcca.dataProcessing.DataFilter.ipmi;

import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.utils.AppLogUtils;
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
 * @description TODO 管理口传感器信息过滤处理类
 * @className IpmiSensorFilterHandler
 * @date 2023/10/27 9:43
 * @since 2.1.0.0
 */
@Component("ipmiSaveFilterHandler")
public class IpmiSaveFilterHandler extends IFilterHandler<CollectSensorEntity> {

    @Resource
    private CollectSensorService sensorService;

    @Override
    public boolean handler(CollectSensorEntity info) {
        AppLogUtils.buildLogInfo(LogFunctionEnum.DATA_PROCESS_SINGLE, "保存管理口数据", info.getAssetIp());
        Date date = new Date();
        if (info.getCollectTime() != null) {
            date.setTime(info.getCollectTime());
        }

        CollectSensor sensor = EntityBeanUtil.copy(info, CollectSensor.class);
        sensor.setCollectCode(info.getCollectCode());
        sensor.setCollectTime(date);
        sensor.setId(MyIdUtil.getId());
        sensorService.save(sensor);
        return true;
    }

    @Override
    public boolean isNeedNextHandle(Boolean flag) {
        return flag;
    }

}
