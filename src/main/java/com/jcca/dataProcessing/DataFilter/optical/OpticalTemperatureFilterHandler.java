package com.jcca.dataProcessing.DataFilter.optical;

import com.jcca.common.utils.AppMathUtil;
import com.jcca.dataProcessing.Entity.ChangeInfo;
import com.jcca.dataProcessing.Entity.OpticalSwitchEntity;
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
import java.util.Date;
import java.util.Map;
import java.util.Set;

/**
 * @author Zhaozheng
 * @description TODO 光纤交换机温度信息过滤处理类
 * @className OpticalTemperatureFilterHandler
 * @date 2023/10/27 9:48
 * @since 2.1.0.0
 */
@Component("opticalTemperatureFilterHandler")
public class OpticalTemperatureFilterHandler extends IFilterHandler<OpticalSwitchEntity> {

    @Resource
    private IEventInfoManagerService eventInfoChangeManagerService;
    @Resource
    private ThresholdManager thresholdManager;

    @Override
    public boolean handler(OpticalSwitchEntity info) {
        Map<String, String> temperatureMap = info.getTemperatureMap();
        if (temperatureMap == null) {
            return true;
        }
        Set<String> keyList = temperatureMap.keySet();

        String redisKey = info.getAssetIp() + ":" + info.getAssetId() + ":" + StatusInfoChangeTypeEnum.status_temp.getCode();
        ThresholdBaseEntity threshold = thresholdManager.getThresholdValue(StatusInfoChangeTypeEnum.event_temp_state.getCode(), info.getAssetId(), null);

        for (String key : keyList) {
            String value = temperatureMap.get(key);
            String mapKey = "temperature" + key;
            boolean flag = eventInfoChangeManagerService.infoIschange(info.getInspectRecordId(),redisKey, mapKey, value);
            ChangeInfo changeInfo = new ChangeInfo();
            changeInfo.setValue(value);
            changeInfo.setRedisKey(redisKey);
            changeInfo.setMapKey(mapKey);
            changeInfo.setCollectTime(new Date());
            info.getMaps().put(mapKey, changeInfo);


            String eventRedisKey = StatusInfoChangeTypeEnum.event_temp_state_normal.getCode();
            String eventMapKey = info.getAssetIp() + "_" + info.getAssetId() + "_" + mapKey;
            String redisThresholdKey =thresholdManager.getThresholdRedisKey(info.getAssetId(),info.getAssetIp());
            String thresholdMapKey = thresholdManager.getThresholdMapKey(StatusInfoChangeTypeEnum.event_temp_state_normal.getCode(),StatusInfoChangeTypeEnum.NORMAL.getCode(),"temperature" + key);

            if (threshold.baseValueIsNull()) {
                IEvent event = eventInfoChangeManagerService.creatRecoveryThresholdEvent(info.getAssetId(), changeInfo, eventRedisKey, eventMapKey, redisThresholdKey, thresholdMapKey,info.getInspectRecordId());
                if (event != null) {
                    this.dispatureEvent(event);
                }
                continue;
            }

            boolean thresholdFlag = eventInfoChangeManagerService.infoIschange(info.getInspectRecordId(),redisThresholdKey, thresholdMapKey, threshold.getBaseValue());
            if (thresholdFlag) {
                this.addThresholdStatus(redisThresholdKey,thresholdMapKey, threshold.getBaseValue(), info);
            }

            if (changeInfo.getIsChange() || thresholdFlag) {
                Boolean compare = AppMathUtil.compare(value, threshold.getBaseValue() + "");
                Integer status = compare ? EventLevelEnum.ABNORMAL.getCode() : EventLevelEnum.NORMAL.getCode();

                String keyWord=status==EventLevelEnum.NORMAL.getCode()?"":"超过";
                AlarmTempReq alarmTempReq = new AlarmTempReq();
                alarmTempReq.setOrgMsg(String.format(StatusInfoChangeTypeEnum.event_temp_state_normal.getDescr(), mapKey, changeInfo.getValue(), keyWord,threshold.getBaseValue()));
                alarmTempReq.setCollectValue(changeInfo.getValue()+"度");
                alarmTempReq.setThresholdValue(threshold.getBaseValue()+"度");
                alarmTempReq.setFlag(mapKey);
                this.addEventStatus(StatusInfoChangeTypeEnum.event_temp_state_normal.getCode(),StatusInfoChangeTypeEnum.NORMAL_VAL.getCode(),"temperature" + key, status, info, changeInfo);
                IEvent event = eventInfoChangeManagerService.creatChangeEvent(info.getAssetId(), changeInfo, eventRedisKey, eventMapKey, status,alarmTempReq,info.getInspectRecordId());
                if (event != null) {
                    //被事件信息截取
                    changeInfo.setIsEvent(true);
                    event.setDescStr(String.format(StatusInfoChangeTypeEnum.event_temp_state_normal.getDescr(), mapKey, changeInfo.getValue(), keyWord,threshold.getBaseValue()));
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
