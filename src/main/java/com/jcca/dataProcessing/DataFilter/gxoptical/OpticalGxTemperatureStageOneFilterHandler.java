package com.jcca.dataProcessing.DataFilter.gxoptical;

import com.jcca.component.thresholds.bean.OpticalSwitchV2Bean;
import com.jcca.component.thresholds.bean.TSensor;
import com.jcca.dataProcessing.Entity.ChangeInfo;
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
import java.util.List;

/**
 * 阶段阈值处理类  中间级别
 */
@Component("opticalGxTemperatureStageOneFilterHandler")
public class OpticalGxTemperatureStageOneFilterHandler extends IFilterHandler<OpticalSwitchV2Bean> {

    @Resource
    private IEventInfoManagerService eventInfoChangeManagerService;
    @Resource
    private ThresholdManager thresholdManager;


    @Override
    public boolean handler(OpticalSwitchV2Bean info) {
        List<TSensor> temperatureMap = info.getTemperatureMap();
        //无温度信息
        if (temperatureMap == null) {
            return true;
        }
        for (TSensor key : temperatureMap) {
            String mapKey = "temperature" + key.getSerialNumberName();
            ChangeInfo changeInfo = info.getMaps().get(mapKey);


            String eventRedisKey = StatusInfoChangeTypeEnum.event_temp_state_sectionOne.getCode();
            String eventMapKey = info.getAssetIp() + "_" + info.getAssetId() + "_" + mapKey;

            String redisThresholdKey =thresholdManager.getThresholdRedisKey(info.getAssetId(),info.getAssetIp());
            String thresholdMapKey = thresholdManager.getThresholdMapKey(StatusInfoChangeTypeEnum.event_temp_state_sectionOne.getCode(),StatusInfoChangeTypeEnum.SECTION_ONE.getCode(),"temperature" + key);

            ThresholdBaseEntity threshold = thresholdManager.getThresholdValue(StatusInfoChangeTypeEnum.event_temp_state.getCode(), info.getAssetId(), mapKey);
            if (threshold.oneLevelIsNull()) {
                IEvent event = eventInfoChangeManagerService.creatRecoveryThresholdEvent(info.getAssetId(), changeInfo, eventRedisKey, eventMapKey, redisThresholdKey, thresholdMapKey,info.getInspectRecordId());
                if (event != null) {
                    this.dispatureEvent(event);
                }

                continue;
            }


            boolean thresholdFlag = eventInfoChangeManagerService.infoIschange(info.getInspectRecordId(),redisThresholdKey, thresholdMapKey, threshold.getOneLevelValue());
            if (thresholdFlag) {
                this.addThresholdStatus(redisThresholdKey,thresholdMapKey, threshold.getBaseValue(), info);
            }

            if (changeInfo.getIsChange() || thresholdFlag) {
                boolean compare = threshold.getOneLevelValue() < Double.valueOf(changeInfo.getValue().toString());
                Integer status = compare ? EventLevelEnum.ABNORMAL.getCode() : EventLevelEnum.NORMAL.getCode();

                String keyWord=status==EventLevelEnum.NORMAL.getCode()?"":"超过";
                AlarmTempReq alarmTempReq = new AlarmTempReq();
                alarmTempReq.setOrgMsg(String.format(StatusInfoChangeTypeEnum.event_temp_state_sectionOne.getDescr(), mapKey, changeInfo.getValue(),keyWord, threshold.getOneLevelValue()));
                alarmTempReq.setCollectValue(changeInfo.getValue()+"度");
                alarmTempReq.setThresholdValue(threshold.getOneLevelValue()+"度");
                alarmTempReq.setFlag(mapKey);
                this.addEventStatus(StatusInfoChangeTypeEnum.event_temp_state_sectionOne.getCode(),StatusInfoChangeTypeEnum.SECTION_ONE_VAL.getCode(),"temperature" + key, status, info, changeInfo);
                IEvent event = eventInfoChangeManagerService.creatChangeEvent(info.getAssetId(), changeInfo, eventRedisKey, eventMapKey, status,alarmTempReq,info.getInspectRecordId(),info.getVersion());


                if (event != null) {
                    //被事件信息截取
                    changeInfo.setIsEvent(true);
                    event.setDescStr(String.format(StatusInfoChangeTypeEnum.event_temp_state_sectionOne.getDescr(), mapKey, changeInfo.getValue(),keyWord, threshold.getOneLevelValue()));
                    this.dispatureEvent(event);
                }
            }
        }
        return true;
    }

    @Override
    public boolean isNeedNextHandle(Boolean flag) {
        return flag;
    }



}
