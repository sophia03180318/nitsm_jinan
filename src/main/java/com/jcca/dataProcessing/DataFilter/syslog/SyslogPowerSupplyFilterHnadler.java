package com.jcca.dataProcessing.DataFilter.syslog;

import com.jcca.common.utils.MyIdUtil;
import com.jcca.dataProcessing.Entity.ChangeInfo;
import com.jcca.dataProcessing.Entity.EventInfo;
import com.jcca.dataProcessing.Entity.SyslogEventInfoEntity;
import com.jcca.dataProcessing.enums.StatusInfoChangeTypeEnum;
import com.jcca.dataProcessing.manager.IEventInfoManagerService;
import com.jcca.dataProcessing.manager.bean.AlarmTempReq;
import com.jcca.dataProcessing.support.IEvent;
import com.jcca.dataProcessing.support.IFilterHandler;
import com.jcca.web.event.enums.EventLevelEnum;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;

/**
 * @author: hhw
 * @description: SyslogPowerSupplyFilterHnadler主要是用来
 * @date: 2025-03-24  15:32
 * @since: 2.1.4.0
 */
@Component("syslogPowerSupplyFilterHnadler")
public class SyslogPowerSupplyFilterHnadler extends IFilterHandler<SyslogEventInfoEntity> {

    @Resource
    private IEventInfoManagerService eventInfoChangeManagerService;

    @Override
    public boolean handler(SyslogEventInfoEntity info) throws Exception {
        String content = info.getMessage();
        if (!((content.contains("Non-redundant") && content.contains("deasserted"))
                || (content.contains("Power Supply") && content.contains("lost")))) {
            return true;
        }

        StatusInfoChangeTypeEnum redisKeyStatus = StatusInfoChangeTypeEnum.event_syslog_power;
        String redisKey = info.getAssetIp() + ":" + info.getAssetId() + ":" + redisKeyStatus.getCode();
        String eventMapKey = info.getAssetIp() + "_" + info.getAssetId() + "_" + new Date().getTime();

        EventInfo eventInfo = new EventInfo();
        eventInfo.setMessage(info.getMessage());
        eventInfo.setMessageId(MyIdUtil.getId());
        eventInfo.setLevel(info.getLevel());

        String mapKey = eventInfo.getMessageId();

        ChangeInfo changeInfo = new ChangeInfo();
        changeInfo.setEventInfo(eventInfo);
        changeInfo.setRedisKey(redisKey);
        changeInfo.setMapKey(mapKey);
        changeInfo.setCollectTime(new Date());
        info.getMaps().put(mapKey, changeInfo);
        String eventRedisKey = StatusInfoChangeTypeEnum.event_syslog_power.getCode();
        Integer status = EventLevelEnum.ABNORMAL.getCode();

        AlarmTempReq tempReq = new AlarmTempReq();
        tempReq.setAssetIp(info.getAssetIp());
        tempReq.setOrgMsg("电源模块冗余电源丢失");

        IEvent event = eventInfoChangeManagerService.creatChangeEvent(info.getAssetId(), changeInfo, eventRedisKey, eventMapKey, status, tempReq);
        if (event != null) {
            //被事件信息截取
            changeInfo.setIsEvent(true);
            event.setDescLog(String.format(StatusInfoChangeTypeEnum.event_syslog_power.getDescr(), eventInfo.getMessage()));
            this.dispatureEvent(event);
        }

        return true;
    }

    @Override
    public boolean isNeedNexthandle(Boolean flag) {
        return flag;
    }
}
