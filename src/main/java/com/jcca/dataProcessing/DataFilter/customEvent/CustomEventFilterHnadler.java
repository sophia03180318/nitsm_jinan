package com.jcca.dataProcessing.DataFilter.customEvent;

import cn.hutool.core.util.ObjectUtil;
import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.utils.AppLogUtils;
import com.jcca.dataProcessing.Entity.ChangeInfo;
import com.jcca.dataProcessing.Entity.CustomEvent;
import com.jcca.dataProcessing.Entity.EventInfo;
import com.jcca.dataProcessing.manager.DataProcessManager;
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
    @Resource
    private DataProcessManager dataProcessManager;

    @Override
    public boolean handler(CustomEvent info) {
        AppLogUtils.buildLogInfo(LogFunctionEnum.DATA_PROCESS_SINGLE, "customEventFilterHnadler", info.getAssetIp());
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
        IEvent event = eventInfoChangeManagerService.creatChangeEvent(info.getAssetId(), changeInfo, eventRedisKey, eventMapKey, EventLevelEnum.ABNORMAL.getCode(),alarmTempReq,info.getInspectRecordId(),info.getVersion());
        event.setDescStr(eventInfo.getMessage());


        try {
           // dataProcessManager.evntInfoHandlerRequest(event);
            this.dispatureEvent(event);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }

    @Override
    public boolean isNeedNextHandle(Boolean flag) {
        return flag;
    }



}
