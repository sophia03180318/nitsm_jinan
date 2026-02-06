package com.jcca.dataProcessing.DataFilter.gxoptical;

import com.jcca.component.thresholds.bean.OpticalSwitchV2Bean;
import com.jcca.component.thresholds.bean.TSensor;
import com.jcca.dataProcessing.Entity.ChangeInfo;
import com.jcca.dataProcessing.enums.StatusInfoChangeTypeEnum;
import com.jcca.dataProcessing.manager.IEventInfoManagerService;
import com.jcca.dataProcessing.manager.bean.AlarmTempReq;
import com.jcca.dataProcessing.support.IEvent;
import com.jcca.dataProcessing.support.IFilterHandler;
import com.jcca.web.event.enums.EventLevelEnum;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * 光纤交换机风扇信息过滤处理类
 */
@Component("opticalGxFanFilterHandler")
public class OpticalGxFanFilterHandler extends IFilterHandler<OpticalSwitchV2Bean> {

    @Resource
    private IEventInfoManagerService eventInfoChangeManagerService;

    @Override
    public boolean handler(OpticalSwitchV2Bean info) {
        String redisKey = info.getAssetIp() + ":" + info.getAssetId() + ":" + StatusInfoChangeTypeEnum.status_fan.getCode();

        List<TSensor> fanStateMap1 = info.getFanStateMap();
        for (TSensor fan : fanStateMap1) {
            int fanState = fan.getStatus();
            String mapKey = "FAN" + fan.getSerialNumberName();
            boolean flag = eventInfoChangeManagerService.infoIschange(info.getInspectRecordId(), redisKey, mapKey, fanState);
            if (flag) {
                ChangeInfo changeInfo = new ChangeInfo();
                changeInfo.setValue(fanState);
                changeInfo.setRedisKey(redisKey);
                changeInfo.setCollectTime(new Date());
                changeInfo.setMapKey(mapKey);
                info.getMaps().put(mapKey, changeInfo);
                String eventRedisKey = StatusInfoChangeTypeEnum.event_fan_state.getCode();
                String eventMapKey = info.getAssetIp() + "_" + info.getAssetId() + "_" + mapKey;

                String str = (fanState == EventLevelEnum.ABNORMAL.getCode()) ? "异常。" : "恢复。";
                AlarmTempReq alarmTempReq = new AlarmTempReq();
                alarmTempReq.setOrgMsg(String.format(StatusInfoChangeTypeEnum.event_fan_state.getDescr(), mapKey, str));
                alarmTempReq.setCollectValue(changeInfo.getValue().toString());
                alarmTempReq.setFlag(mapKey);
                this.addEventStatus(StatusInfoChangeTypeEnum.status_fan.getCode(), StatusInfoChangeTypeEnum.STATUS.getCode(), "FAN" + fan, fanState, info, changeInfo);
                IEvent event = eventInfoChangeManagerService.creatChangeEvent(info.getAssetId(), changeInfo, eventRedisKey, eventMapKey, fanState, alarmTempReq, info.getInspectRecordId(), info.getVersion());
                if (event != null) {
                    //被事件信息截取
                    changeInfo.setIsEvent(true);

                    event.setDescStr(String.format(StatusInfoChangeTypeEnum.event_fan_state.getDescr(), mapKey, str));
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
