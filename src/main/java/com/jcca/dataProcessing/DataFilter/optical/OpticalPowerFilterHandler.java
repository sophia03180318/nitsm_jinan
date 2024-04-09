package com.jcca.dataProcessing.DataFilter.optical;

import com.jcca.dataProcessing.Entity.ChangeInfo;
import com.jcca.dataProcessing.Entity.OpticalSwitchEntity;
import com.jcca.dataProcessing.enums.StatusInfoChangeTypeEnum;
import com.jcca.dataProcessing.manager.IEventInfoManagerService;
import com.jcca.dataProcessing.manager.bean.AlarmTempReq;
import com.jcca.dataProcessing.support.IEvent;
import com.jcca.dataProcessing.support.IFilterHandler;
import com.jcca.web.event.enums.EventLevelEnum;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * @author Zhaozheng
 * @description TODO 光纤交换机风扇信息过滤处理类
 * @className OpticalFanFilterHandler
 * @date 2023/10/27 9:48
 * @since 2.1.0.0
 */
@Component("opticalPowerFilterHandler")
public class OpticalPowerFilterHandler extends IFilterHandler<OpticalSwitchEntity> {

    @Resource
    private IEventInfoManagerService eventInfoChangeManagerService;

    @Override
    public boolean handler(OpticalSwitchEntity info) {
        Map<String, String> pwrStateMap = info.getPwrStateMap();
        if (Objects.isNull(pwrStateMap)) {
            return true;
        }
        String redisKey = info.getAssetIp() + ":" + info.getAssetId() + ":" + StatusInfoChangeTypeEnum.status_power.getCode();

        Set<String> fanIndex = pwrStateMap.keySet();
        for (String power : fanIndex) {
            String mapKey = "power" + power;
            boolean flag = eventInfoChangeManagerService.infoIschange(redisKey, mapKey, pwrStateMap.get(power));
            if (flag) {
                ChangeInfo changeInfo = new ChangeInfo();
                changeInfo.setValue(pwrStateMap.get(power));
                changeInfo.setRedisKey(redisKey);
                changeInfo.setCollectTime(new Date(info.getCollectTime()));
                changeInfo.setMapKey(mapKey);
                info.getMaps().put(mapKey, changeInfo);
                String eventRedisKey = StatusInfoChangeTypeEnum.event_power_state.getCode();
                String eventMapKey = info.getAssetIp() + "_" + info.getAssetId() + "_" + mapKey;

                boolean ok = pwrStateMap.get(power).toUpperCase().contains("OK");
                Integer status = ok ? EventLevelEnum.NORMAL.getCode() : EventLevelEnum.ABNORMAL.getCode();
                this.addEventStatus(StatusInfoChangeTypeEnum.event_power_state.getCode(),StatusInfoChangeTypeEnum.STATUS.getCode(),mapKey, status, info, changeInfo);
                String str = status == EventLevelEnum.ABNORMAL.getCode() ? "异常。" : "恢复。";
                AlarmTempReq alarmTempReq = new AlarmTempReq();
                alarmTempReq.setOrgMsg(String.format(StatusInfoChangeTypeEnum.event_power_state.getDescr(), mapKey, str));
                alarmTempReq.setCollectValue(changeInfo.getValue().toString());
                alarmTempReq.setFlag(power);
                IEvent event = eventInfoChangeManagerService.creatChangeEvent(info.getAssetId(), changeInfo, eventRedisKey, eventMapKey, status,alarmTempReq);
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
    public boolean isNeedNexthandle(Boolean flag) {
        return flag;
    }


}
