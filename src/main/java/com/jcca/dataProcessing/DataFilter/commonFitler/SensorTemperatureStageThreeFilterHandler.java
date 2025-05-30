package com.jcca.dataProcessing.DataFilter.commonFitler;

import com.jcca.dataProcessing.Entity.ChangeInfo;
import com.jcca.dataProcessing.Entity.CollectSensorEntity;
import com.jcca.dataProcessing.Entity.ThresholdBaseEntity;
import com.jcca.dataProcessing.enums.StatusInfoChangeTypeEnum;
import com.jcca.dataProcessing.manager.IEventInfoManagerService;
import com.jcca.dataProcessing.manager.bean.AlarmTempReq;
import com.jcca.dataProcessing.manager.threshold.ThresholdManager;
import com.jcca.dataProcessing.support.IEvent;
import com.jcca.dataProcessing.support.IFilterHandler;
import com.jcca.web.event.enums.EventLevelEnum;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @description: 阶段阈值处理类  最高级别
 * @author: Lvyp
 * @create: 2023/11/02 20:13
 */
@Component("sensorTemperatureStageThreeFilterHandler")
public class SensorTemperatureStageThreeFilterHandler extends IFilterHandler<CollectSensorEntity> {

    @Resource
    private IEventInfoManagerService eventInfoChangeManagerService;
    @Resource
    private ThresholdManager thresholdManager;

    @Override
    public boolean handler(CollectSensorEntity info) {

        ChangeInfo changeInfo = info.getMaps().get(StatusInfoChangeTypeEnum.status_tempValue.getCode());
        if (changeInfo == null) {
            return true;
        }

        String eventRedisKey = StatusInfoChangeTypeEnum.event_temp_state_sectionThree.getCode();
        String eventMapKey = info.getAssetIp() + "_" + info.getAssetId() + "_" + info.getSerialNumberName();
        String redisThresholdKey = thresholdManager.getThresholdRedisKey(info.getAssetId(), info.getAssetIp());
        String thresholdMapKey = thresholdManager.getThresholdMapKey(StatusInfoChangeTypeEnum.event_temp_state_sectionThree.getCode(), StatusInfoChangeTypeEnum.SECTION_THREE.getCode(), info.getName());

        ThresholdBaseEntity threshold = thresholdManager.getThresholdValue(StatusInfoChangeTypeEnum.event_temp_state_normal.getCode(), info.getAssetId(), null);
        if (threshold.threeLevelIsNull()) {
            IEvent event = eventInfoChangeManagerService.creatRecoveryThresholdEvent(info.getAssetId(), changeInfo, eventRedisKey, eventMapKey, redisThresholdKey, thresholdMapKey,info.getInspectRecordId());
            if (event != null) {
                this.dispatureEvent(event);
            }
            return true;
        }

        boolean thresholdFlag = eventInfoChangeManagerService.infoIschange(info.getInspectRecordId(),redisThresholdKey, thresholdMapKey, threshold.getThreeLevelValue());
        if (thresholdFlag) {
            this.addThresholdStatus(redisThresholdKey, thresholdMapKey , threshold.getThreeLevelValue(), info);
        }

        //if (thresholdFlag) {
        if (true) {
            boolean compare = threshold.getThreeLevelValue() < Double.valueOf(changeInfo.getValue().toString());
            Integer status = compare ? EventLevelEnum.ABNORMAL.getCode() : EventLevelEnum.NORMAL.getCode();
            String keyWord = status == EventLevelEnum.NORMAL.getCode() ? "" : "超过";
            AlarmTempReq alarmTempReq = new AlarmTempReq();
            alarmTempReq.setOrgMsg(String.format(StatusInfoChangeTypeEnum.event_temp_state_sectionThree.getDescr(), info.getSerialNumberName(), changeInfo.getValue(), keyWord, threshold.getThreeLevelValue()));
            alarmTempReq.setCollectValue(changeInfo.getValue() + "");
            alarmTempReq.setThresholdValue(threshold.getThreeLevelValue() + "度");
            alarmTempReq.setFlag(info.getName());
            this.addEventStatus(StatusInfoChangeTypeEnum.event_temp_state_sectionThree.getCode(), StatusInfoChangeTypeEnum.SECTION_THREE_VAL.getCode(), info.getName(), status, info, changeInfo);
            IEvent event = eventInfoChangeManagerService.creatChangeEvent(info.getAssetId(), changeInfo, eventRedisKey, eventMapKey, status, alarmTempReq,info.getInspectRecordId());
            if (event != null) {
                //被事件信息截取
                changeInfo.setIsEvent(true);

                event.setDescStr(String.format(StatusInfoChangeTypeEnum.event_temp_state_sectionThree.getDescr(), info.getSerialNumberName(), changeInfo.getValue(), keyWord, threshold.getThreeLevelValue()));

                this.dispatureEvent(event);
            }
        }

        return true;
    }

    @Override
    public boolean isNeedNexthandle(Boolean flag) {
        return true;
    }


}
