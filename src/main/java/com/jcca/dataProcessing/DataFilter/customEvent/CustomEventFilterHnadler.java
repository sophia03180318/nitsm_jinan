package com.jcca.dataProcessing.DataFilter.customEvent;

import cn.hutool.core.util.ObjectUtil;
import com.jcca.dataProcessing.Entity.ChangeInfo;
import com.jcca.dataProcessing.Entity.CustomEvent;
import com.jcca.dataProcessing.Entity.EventInfo;
import com.jcca.dataProcessing.manager.IEventInfoManagerService;
import com.jcca.dataProcessing.manager.bean.AlarmTempReq;
import com.jcca.dataProcessing.support.IEvent;
import com.jcca.dataProcessing.support.IFilterHandler;
import com.jcca.web.event.enums.EventLevelEnum;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;


@Component("customEventFilterHnadler")
public class CustomEventFilterHnadler extends IFilterHandler<CustomEvent> {

    @Resource
    private IEventInfoManagerService eventInfoChangeManagerService;

    @Override
    public boolean handler(CustomEvent info) {
        ChangeInfo changeInfo = new ChangeInfo();
        changeInfo.setCollectTime(new Date());
        EventInfo eventInfo = new EventInfo();
        eventInfo.setMessageId(info.getUniqueCode());
        eventInfo.setMessage(info.getMsg());
        changeInfo.setEventInfo(eventInfo);
        String eventRedisKey = info.getType();
        String eventMapKey = info.getAssetIp() + "_" + info.getAssetId();
        AlarmTempReq alarmTempReq = new AlarmTempReq();
        alarmTempReq.setOrgMsg(String.format(eventInfo.getMessage()));
        alarmTempReq.setCollectValue(eventInfo.getEventType());
        if (ObjectUtil.isNotNull(info)){
            alarmTempReq.setFlag(info.getFlag());
        }
        IEvent event = eventInfoChangeManagerService.creatChangeEvent(info.getAssetId(), changeInfo, eventRedisKey, eventMapKey, EventLevelEnum.ABNORMAL.getCode(),alarmTempReq);
        event.setDescStr(eventInfo.getMessage());
        this.dispatureEvent(event);

        return true;
    }

    @Override
    public boolean isNeedNexthandle(Boolean flag) {
        return flag;
    }



}
