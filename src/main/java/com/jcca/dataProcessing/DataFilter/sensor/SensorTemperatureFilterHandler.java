package com.jcca.dataProcessing.DataFilter.sensor;

import com.jcca.common.utils.AppMathUtil;
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
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @description: 温度普通阈值
 * @author: Lvyp
 * @create: 2023/11/07 10:05
 */
@Component("sensorTemperatureFilterHandler")
public class SensorTemperatureFilterHandler extends IFilterHandler<CollectSensorEntity> {

    @Resource
    private IEventInfoManagerService eventInfoChangeManagerService;
    @Resource
    private ThresholdManager thresholdManager;


    @Override
    public boolean handler(CollectSensorEntity info) {
        if (!SensorTypeEnum.GAUGE.name().equals(info.getSensorType())) {
            return true;
        }
        String redisKey = info.getAssetIp() + ":" + info.getAssetId() + ":" + StatusInfoChangeTypeEnum.status_temp.getCode() + ":" + info.getSerialNumberName();

        String mapKey1 = StatusInfoChangeTypeEnum.status_tempStatus.getCode();
        boolean flag1 = eventInfoChangeManagerService.infoIschange(info.getInspectRecordId(),redisKey, mapKey1, info.getStatus());
        if (flag1) {
            ChangeInfo changeInfo1 = new ChangeInfo();
            changeInfo1.setValue(info.getStatus());
            changeInfo1.setRedisKey(redisKey);
            changeInfo1.setMapKey(mapKey1);
            changeInfo1.setCollectTime(new Date(info.getCollectTime()));
            info.getMaps().put(mapKey1, changeInfo1);
            String eventRedisKey1 = StatusInfoChangeTypeEnum.event_temp_state.getCode();
            String eventMapKey1 = info.getAssetIp() + "_" + info.getAssetId() + "_" + info.getSerialNumberName();
            List<String> normalStatusList = Arrays.asList("0", "1");
            Integer status1 = normalStatusList.contains(info.getStatus()) ? EventLevelEnum.NORMAL.getCode() : EventLevelEnum.ABNORMAL.getCode();
            String str = status1 == EventLevelEnum.ABNORMAL.getCode() ? "异常。" : "恢复。";
            AlarmTempReq alarmTempReq = new AlarmTempReq();
            alarmTempReq.setOrgMsg(String.format(StatusInfoChangeTypeEnum.event_temp_state.getDescr(), info.getSerialNumberName(), str));
            alarmTempReq.setCollectValue(changeInfo1.getValue().toString());
            alarmTempReq.setFlag(info.getSerialNumberName());
            this.addEventStatus(StatusInfoChangeTypeEnum.event_temp_state.getCode(), StatusInfoChangeTypeEnum.STATUS.getCode(), info.getName(), status1, info, changeInfo1);
            IEvent event1 = eventInfoChangeManagerService.creatChangeEvent(info.getAssetId(), changeInfo1, eventRedisKey1, eventMapKey1, status1, alarmTempReq,info.getInspectRecordId());
            if (event1 != null) {
                //被事件信息截取
                changeInfo1.setIsEvent(true);

                event1.setDescStr(String.format(StatusInfoChangeTypeEnum.event_temp_state.getDescr(), info.getSerialNumberName(), str));
                this.dispatureEvent(event1);
            }
        }


        String mapKey = StatusInfoChangeTypeEnum.status_tempValue.getCode();

        ChangeInfo changeInfo = new ChangeInfo();
        changeInfo.setValue(info.getValue());
        changeInfo.setRedisKey(redisKey);
        changeInfo.setMapKey(mapKey);
        changeInfo.setCollectTime(new Date(info.getCollectTime()));
        info.getMaps().put(mapKey, changeInfo);

        String eventRedisKey = StatusInfoChangeTypeEnum.event_temp_state_normal.getCode();
        String eventMapKey = info.getAssetIp() + "_" + info.getAssetId() + "_" + info.getSerialNumberName();
        String redisThresholdKey = thresholdManager.getThresholdRedisKey(info.getAssetId(), info.getAssetIp());
        String thresholdMapKey = thresholdManager.getThresholdMapKey(StatusInfoChangeTypeEnum.event_temp_state_normal.getCode(), StatusInfoChangeTypeEnum.NORMAL_VAL.getCode(), info.getName());

        ThresholdBaseEntity threshold = thresholdManager.getThresholdValue(StatusInfoChangeTypeEnum.event_temp_state_normal.getCode(), info.getAssetId(), null);

        if (threshold.baseValueIsNull()) {
            IEvent event = eventInfoChangeManagerService.creatRecoveryThresholdEvent(info.getAssetId(), changeInfo, eventRedisKey, eventMapKey, redisThresholdKey, thresholdMapKey,info.getInspectRecordId());
            if (event != null) {
                this.dispatureEvent(event);
            }
            return true;
        }
        boolean thresholdFlag = eventInfoChangeManagerService.infoIschange(info.getInspectRecordId(),redisThresholdKey, thresholdMapKey, threshold.getBaseValue());
        if (thresholdFlag) {
            this.addThresholdStatus(redisThresholdKey, thresholdMapKey, threshold.getBaseValue(), info);
        }

        Boolean compare = AppMathUtil.compare(info.getValue() + "", threshold.getBaseValue() + "");
        Integer status = compare ? EventLevelEnum.ABNORMAL.getCode() : EventLevelEnum.NORMAL.getCode();
        String keyWord = status == EventLevelEnum.NORMAL.getCode() ? "" : "超过";
        AlarmTempReq alarmTempReq = new AlarmTempReq();
        alarmTempReq.setOrgMsg(String.format(StatusInfoChangeTypeEnum.event_temp_state_normal.getDescr(), info.getSerialNumberName(), changeInfo.getValue(), keyWord, threshold.getBaseValue()));
        alarmTempReq.setCollectValue(changeInfo.getValue() + "度");
        alarmTempReq.setThresholdValue(threshold.getBaseValue() + "度");
        alarmTempReq.setFlag(info.getSerialNumberName());
        this.addEventStatus(StatusInfoChangeTypeEnum.event_temp_state_normal.getCode(), StatusInfoChangeTypeEnum.STATUS.getCode(), info.getName(), status, info, changeInfo);
        IEvent event = eventInfoChangeManagerService.creatChangeEvent(info.getAssetId(), changeInfo, eventRedisKey, eventMapKey, status, alarmTempReq,info.getInspectRecordId());
        if (event != null) {
            //被事件信息截取
            changeInfo.setIsEvent(true);

            event.setDescStr(String.format(StatusInfoChangeTypeEnum.event_temp_state_normal.getDescr(), info.getSerialNumberName(), changeInfo.getValue(), keyWord, threshold.getBaseValue()));
            this.dispatureEvent(event);
        }



        return true;

    }

    @Override
    public boolean isNeedNextHandle(Boolean flag) {
        return flag;
    }


}
