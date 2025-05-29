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
 * @description: 阶段阈值处理类  中间级别
 * @author: Lvyp
 * @create: 2023/11/02 20:13
 */
@Component("sensorTemperatureStageOneFilterHandler")
public class SensorTemperatureStageOneFilterHandler extends IFilterHandler<CollectSensorEntity> {

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

        String eventRedisKey = StatusInfoChangeTypeEnum.event_temp_state_sectionOne.getCode();
        String eventMapKey = info.getAssetIp() + "_" + info.getAssetId() + "_" + info.getSerialNumberName();
        String redisThresholdKey = thresholdManager.getThresholdRedisKey(info.getAssetId(), info.getAssetIp());
        String thresholdMapKey = thresholdManager.getThresholdMapKey(StatusInfoChangeTypeEnum.event_temp_state_sectionOne.getCode(), StatusInfoChangeTypeEnum.SECTION_ONE.getCode(), info.getName());

        ThresholdBaseEntity threshold = thresholdManager.getThresholdValue(StatusInfoChangeTypeEnum.event_temp_state_normal.getCode(), info.getAssetId(), null);
        if (threshold.oneLevelIsNull()) {
            IEvent event = eventInfoChangeManagerService.creatRecoveryThresholdEvent(info.getAssetId(), changeInfo, eventRedisKey, eventMapKey, redisThresholdKey, thresholdMapKey);
            if (event != null) {
                this.dispatureEvent(event);
            }
            return true;
        }

        boolean thresholdFlag = eventInfoChangeManagerService.infoIschange(info.getInspectRecordId(),redisThresholdKey, thresholdMapKey, threshold.getOneLevelValue());
        if (thresholdFlag) {
            this.addThresholdStatus(redisThresholdKey, thresholdMapKey, threshold.getOneLevelValue(), info);
        }
        boolean compare = threshold.getOneLevelValue() < Double.valueOf(changeInfo.getValue().toString());
        //if (thresholdFlag) {
        if (true) {
            Integer status = compare ? EventLevelEnum.ABNORMAL.getCode() : EventLevelEnum.NORMAL.getCode();
            this.addEventStatus(StatusInfoChangeTypeEnum.event_temp_state_sectionOne.getCode(), StatusInfoChangeTypeEnum.SECTION_ONE_VAL.getCode(), info.getName(), status, info, changeInfo);
            String keyWord = status == EventLevelEnum.NORMAL.getCode() ? "" : "超过";
            AlarmTempReq alarmTempReq = new AlarmTempReq();
            alarmTempReq.setOrgMsg(String.format(StatusInfoChangeTypeEnum.event_temp_state_sectionOne.getDescr(), info.getSerialNumberName(), changeInfo.getValue(), keyWord, threshold.getOneLevelValue()));
            alarmTempReq.setCollectValue(changeInfo.getValue()+"");
            alarmTempReq.setThresholdValue(threshold.getOneLevelValue()+"度");
            alarmTempReq.setFlag(info.getName());
            IEvent event = eventInfoChangeManagerService.creatChangeEvent(info.getAssetId(), changeInfo, eventRedisKey, eventMapKey, status,alarmTempReq);
            if (event != null) {
                //被事件信息截取
                changeInfo.setIsEvent(true);

                event.setDescStr(String.format(StatusInfoChangeTypeEnum.event_temp_state_sectionOne.getDescr(), info.getSerialNumberName(), changeInfo.getValue(), keyWord, threshold.getOneLevelValue()));

                this.dispatureEvent(event);
            }

        }
        //一阶、二阶、三阶阈值告警信息，命中哪一个就是哪一个不会再命中其他的处理类
        if (compare) {//触发异常时，不需要进入二阶
            return false;
        }

        return true;
    }

    @Override
    public boolean isNeedNexthandle(Boolean flag) {
        return flag;
    }


}
