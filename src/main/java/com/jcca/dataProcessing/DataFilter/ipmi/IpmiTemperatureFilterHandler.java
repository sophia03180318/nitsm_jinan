package com.jcca.dataProcessing.DataFilter.ipmi;

import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.utils.AppLogUtils;
import com.jcca.dataProcessing.Entity.ChangeInfo;
import com.jcca.dataProcessing.Entity.CollectSensorEntity;
import com.jcca.dataProcessing.Entity.ThresholdBaseEntity;
import com.jcca.dataProcessing.enums.StatusInfoChangeTypeEnum;
import com.jcca.dataProcessing.manager.IEventInfoManagerService;
import com.jcca.dataProcessing.manager.bean.AlarmTempReq;
import com.jcca.dataProcessing.manager.threshold.ThresholdManager;
import com.jcca.dataProcessing.support.IEvent;
import com.jcca.dataProcessing.support.IFilterHandler;
import com.jcca.web.collect.enums.SensorTypeEnum;
import com.jcca.web.event.enums.EventLevelEnum;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;
import java.util.Objects;

/**
 * @description: 温度普通阈值
 * @author: Lvyp
 * @create: 2023/11/07 10:05
 */
@Component("ipmiTemperatureFilterHandler")
public class IpmiTemperatureFilterHandler extends IFilterHandler<CollectSensorEntity> {

    @Resource
    private IEventInfoManagerService eventInfoChangeManagerService;
    @Resource
    private ThresholdManager thresholdManager;


    @Override
    public boolean handler(CollectSensorEntity info) {
        if (!SensorTypeEnum.GAUGE.name().equals(info.getSensorType())) {
            return true;
        }
        AppLogUtils.buildLogInfo(LogFunctionEnum.DATA_PROCESS_SINGLE, "管理口温度信息过滤处理类", info.getAssetIp());
        String redisKey = info.getAssetIp() + ":" + info.getAssetId() + ":" + StatusInfoChangeTypeEnum.status_temp.getCode() + ":" + info.getSerialNumberName();
        String mapKey = StatusInfoChangeTypeEnum.status_tempValue.getCode();
        boolean flag = eventInfoChangeManagerService.infoIschange(redisKey, mapKey, info.getValue());
        ChangeInfo changeInfo = new ChangeInfo();
        changeInfo.setValue(info.getValue());
        changeInfo.setRedisKey(redisKey);
        changeInfo.setMapKey(mapKey);
        changeInfo.setCollectTime(new Date());
        info.getMaps().put(mapKey, changeInfo);


        String eventRedisKey = StatusInfoChangeTypeEnum.event_temp_state_normal.getCode();
        String eventMapKey = info.getAssetIp() + "_" + info.getAssetId() + "_" + info.getSerialNumberName();
        String redisThresholdKey = thresholdManager.getThresholdRedisKey(info.getAssetId(), info.getAssetIp());
        String thresholdMapKey = thresholdManager.getThresholdMapKey(StatusInfoChangeTypeEnum.event_temp_state_normal.getCode(), StatusInfoChangeTypeEnum.NORMAL.getCode(), info.getSerialNumberName());


        ThresholdBaseEntity threshold = thresholdManager.getThresholdValue(StatusInfoChangeTypeEnum.event_temp_state_normal.getCode(), info.getAssetId(), null);
        if (threshold.baseValueIsNull()) {
            if(Objects.nonNull(changeInfo)){
                IEvent event = eventInfoChangeManagerService.creatRecoveryThresholdEvent(info.getAssetId(), changeInfo, eventRedisKey, eventMapKey, redisThresholdKey, thresholdMapKey);
                if (event != null) {
                    this.dispatureEvent(event);
                }
            }
            return true;
        }

        boolean thresholdFlag = eventInfoChangeManagerService.infoIschange(redisThresholdKey, thresholdMapKey, threshold.getBaseValue());
        if (thresholdFlag) {
            this.addThresholdStatus(redisThresholdKey, thresholdMapKey, threshold.getBaseValue(), info);
        }


        boolean compare = threshold.getBaseValue() < Double.valueOf(changeInfo.getValue().toString());
        Integer status = compare ? EventLevelEnum.ABNORMAL.getCode() : EventLevelEnum.NORMAL.getCode();
        String keyWord = status == EventLevelEnum.NORMAL.getCode() ? "" : "超过";

        String orgMsg = String.format(StatusInfoChangeTypeEnum.event_temp_state_normal.getDescr(), info.getSerialNumberName(), changeInfo.getValue(), keyWord, threshold.getBaseValue());
        AlarmTempReq alarmTempReq = new AlarmTempReq();
        alarmTempReq.setOrgMsg(orgMsg);
        alarmTempReq.setCollectValue(changeInfo.getValue() + "");
        alarmTempReq.setThresholdValue(threshold.getBaseValue() + "度");
        alarmTempReq.setFlag(info.getName());

        this.addEventStatus(StatusInfoChangeTypeEnum.event_temp_state_normal.getCode(), StatusInfoChangeTypeEnum.NORMAL_VAL.getCode(), info.getName(), status, info, changeInfo);
        IEvent event = eventInfoChangeManagerService.creatChangeEvent(info.getAssetId(), changeInfo, eventRedisKey, eventMapKey, status, alarmTempReq);
        if (event != null) {
            //被事件信息截取
            changeInfo.setIsEvent(true);
            event.setDescStr(orgMsg);
            this.dispatureEvent(event);
        }


        return true;
    }

    @Override
    public boolean isNeedNexthandle(Boolean flag) {
        return flag;
    }


}
