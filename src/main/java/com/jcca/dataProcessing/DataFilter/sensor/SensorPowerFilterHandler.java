package com.jcca.dataProcessing.DataFilter.sensor;

import com.jcca.dataProcessing.Entity.ChangeInfo;
import com.jcca.dataProcessing.Entity.CollectSensorEntity;
import com.jcca.dataProcessing.enums.StatusInfoChangeTypeEnum;
import com.jcca.dataProcessing.manager.IEventInfoManagerService;
import com.jcca.dataProcessing.manager.bean.AlarmTempReq;
import com.jcca.dataProcessing.support.IEvent;
import com.jcca.dataProcessing.support.IFilterHandler;
import com.jcca.web.collect.enums.SensorTypeEnum;
import com.jcca.web.event.enums.EventLevelEnum;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @author Zhaozheng
 * @description TODO 传感器电源过滤处理类
 * @className sensorStateFilterHandler
 * @date 2023/10/27 9:59
 * @since 2.1.0.0
 */
@Component("sensorPowerFilterHandler")
public class SensorPowerFilterHandler extends IFilterHandler<CollectSensorEntity> {

    @Resource
    private IEventInfoManagerService eventInfoChangeManagerService;

    @Override
    public boolean handler(CollectSensorEntity info) {
        if (!SensorTypeEnum.POWER.name().equals(info.getSensorType())) {
            return true;
        }
        String redisKey = info.getAssetIp() + ":" + info.getAssetId() + ":" + StatusInfoChangeTypeEnum.status_power.getCode() + ":" + info.getSerialNumberName();
        String mapKey1 = StatusInfoChangeTypeEnum.status_power_state.getCode();
        boolean flag1 = eventInfoChangeManagerService.infoIschange(info.getInspectRecordId(),redisKey, mapKey1, info.getStatus());
        if (flag1) {
            ChangeInfo changeInfo = new ChangeInfo();
            changeInfo.setValue(info.getStatus());
            changeInfo.setRedisKey(redisKey);
            changeInfo.setCollectTime(new Date(info.getCollectTime()));
            changeInfo.setMapKey(mapKey1);
            changeInfo.setCollectTime(new Date(info.getCollectTime()));
            info.getMaps().put(mapKey1, changeInfo);
            String eventRedisKey = StatusInfoChangeTypeEnum.event_power_state.getCode();
            String eventMapKey = info.getAssetIp() + "_" + info.getAssetId() + "_" + info.getSerialNumberName();
            List<String> normalStatusList = Arrays.asList("0", "1");
            Integer status = normalStatusList.contains(info.getStatus()) ? EventLevelEnum.NORMAL.getCode() : EventLevelEnum.ABNORMAL.getCode();
            String str = status == EventLevelEnum.ABNORMAL.getCode() ? "异常。" : "恢复。";
            AlarmTempReq alarmTempReq = new AlarmTempReq();
            alarmTempReq.setOrgMsg(String.format(StatusInfoChangeTypeEnum.event_power_state.getDescr(), info.getSerialNumberName(), str));
            alarmTempReq.setCollectValue(changeInfo.getValue().toString());
            alarmTempReq.setFlag(info.getSerialNumberName());
            this.addEventStatus(StatusInfoChangeTypeEnum.event_power_state.getCode(),StatusInfoChangeTypeEnum.STATUS.getCode(),info.getName(), status, info, changeInfo);
            IEvent event = eventInfoChangeManagerService.creatChangeEvent(info.getAssetId(), changeInfo, eventRedisKey, eventMapKey, status,alarmTempReq,info.getInspectRecordId());
            if (event != null) {
                //被事件信息截取
                changeInfo.setIsEvent(true);

                event.setDescStr(String.format(StatusInfoChangeTypeEnum.event_power_state.getDescr(), info.getSerialNumberName(), str));
                this.dispatureEvent(event);
            }
        }

        return true;


    }

    @Override
    public boolean isNeedNexthandle(Boolean flag) {
        return flag;
    }


}
