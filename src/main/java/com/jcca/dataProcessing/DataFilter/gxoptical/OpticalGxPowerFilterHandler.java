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
import java.util.Objects;

/**
 * 光纤交换机风扇信息过滤处理类
 */
@Component("opticalGxPowerFilterHandler")
public class OpticalGxPowerFilterHandler extends IFilterHandler<OpticalSwitchV2Bean> {

    @Resource
    private IEventInfoManagerService eventInfoChangeManagerService;

    @Override
    public boolean handler(OpticalSwitchV2Bean info) {
        List<TSensor> pwrStateMap = info.getPwrStateMap();
        if (Objects.isNull(pwrStateMap)) {
            return true;
        }
        String redisKey = info.getAssetIp() + ":" + info.getAssetId() + ":" + StatusInfoChangeTypeEnum.status_power.getCode();

        for (TSensor power : pwrStateMap) {
            String mapKey = "power" + power;
            Integer value = Integer.valueOf(power.getValue());
            boolean flag = eventInfoChangeManagerService.infoIschange(info.getInspectRecordId(), redisKey, mapKey, value);
            if (flag) {
                ChangeInfo changeInfo = new ChangeInfo();
                changeInfo.setValue(value);
                changeInfo.setRedisKey(redisKey);
                changeInfo.setCollectTime(new Date(info.getCollectTime()));
                changeInfo.setMapKey(mapKey);
                info.getMaps().put(mapKey, changeInfo);
                String eventRedisKey = StatusInfoChangeTypeEnum.event_power_state.getCode();
                String eventMapKey = info.getAssetIp() + "_" + info.getAssetId() + "_" + mapKey;

                Integer status = value.equals(EventLevelEnum.NORMAL.getCode()) ? EventLevelEnum.NORMAL.getCode() : EventLevelEnum.ABNORMAL.getCode();
                this.addEventStatus(StatusInfoChangeTypeEnum.event_power_state.getCode(), StatusInfoChangeTypeEnum.STATUS.getCode(), mapKey, status, info, changeInfo);
                String str = Objects.equals(status, EventLevelEnum.ABNORMAL.getCode()) ? "异常。" : "恢复。";
                AlarmTempReq alarmTempReq = new AlarmTempReq();
                alarmTempReq.setOrgMsg(String.format(StatusInfoChangeTypeEnum.event_power_state.getDescr(), mapKey, str));
                alarmTempReq.setCollectValue(changeInfo.getValue().toString());
                alarmTempReq.setFlag(power.getSerialNumberName()); // TODO
                IEvent event = eventInfoChangeManagerService.creatChangeEvent(info.getAssetId(), changeInfo, eventRedisKey, eventMapKey, status, alarmTempReq, info.getInspectRecordId(), info.getVersion());
                if (event != null) {
                    //被事件信息截取
                    changeInfo.setIsEvent(true);
                    event.setDescStr(String.format(StatusInfoChangeTypeEnum.event_power_state.getDescr(), mapKey, str));
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
