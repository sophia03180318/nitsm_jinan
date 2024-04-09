package com.jcca.dataProcessing.DataFilter.ipmi;

import com.jcca.dataProcessing.Entity.ChangeInfo;
import com.jcca.dataProcessing.Entity.CollectSensorEntity;
import com.jcca.dataProcessing.enums.StatusInfoChangeTypeEnum;
import com.jcca.dataProcessing.manager.IEventInfoManagerService;
import com.jcca.dataProcessing.support.IFilterHandler;
import com.jcca.web.collect.enums.SensorTypeEnum;
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
@Component("ipmiLogFilterHandler")
public class IpmiLogFilterHandler extends IFilterHandler<CollectSensorEntity> {

    @Resource
    private IEventInfoManagerService eventInfoChangeManagerService;

    @Override
    public boolean handler(CollectSensorEntity info) {
        if (!SensorTypeEnum.LOG.name().equals(info.getSensorType())) {
            return true;
        }
        String redisKey = info.getAssetIp() + ":" + info.getAssetId() + ":" + StatusInfoChangeTypeEnum.status_ipmi_log.getCode();
        String mapKey = info.getValue() + "_" + info.getStatus();
        boolean flag = eventInfoChangeManagerService.infoIschange(redisKey, mapKey, info.getValue());
        if (flag) {
            ChangeInfo changeInfo = new ChangeInfo();
            changeInfo.setValue(info.getMsg());
            changeInfo.setRedisKey(redisKey);
            changeInfo.setCollectTime(new Date());
            changeInfo.setMapKey(mapKey);
            info.getMaps().put(mapKey, changeInfo);
        }
        return true;
    }

    @Override
    public boolean isNeedNexthandle(Boolean flag) {
        return flag;
    }

}
