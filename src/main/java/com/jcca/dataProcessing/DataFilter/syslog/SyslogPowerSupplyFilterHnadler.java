package com.jcca.dataProcessing.DataFilter.syslog;

import com.jcca.common.config.thymeleaf.utility.DictUtil;
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
import java.util.Map;
import java.util.Set;

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
        int power = isPower(content);
        if (power == 0) {
            return true;
        }

        info.setMessage(content + "电源丢失");
        String msg = "电源模块冗余电源丢失";
        Integer status = EventLevelEnum.ABNORMAL.getCode();
        if (power == 2) {
            status = EventLevelEnum.NORMAL.getCode();
            msg = "电源模块冗余电源恢复";
            info.setMessage(content + "电源恢复");
        }

        StatusInfoChangeTypeEnum redisKeyStatus = StatusInfoChangeTypeEnum.event_syslog_power;
        String redisKey = info.getAssetIp() + ":" + info.getAssetId() + ":" + redisKeyStatus.getCode();
        String mapKey = info.getAssetIp() + "_" + info.getAssetId() + "_syslog_power";

        Boolean flag = eventInfoChangeManagerService.infoIschangeFirst(info.getInspectRecordId(),redisKey, mapKey, status == 2);
        if (flag == null || flag) {
            EventInfo eventInfo = new EventInfo();
            eventInfo.setMessage(info.getMessage());
            eventInfo.setMessageId(MyIdUtil.getId());
            eventInfo.setLevel(info.getLevel());

            ChangeInfo changeInfo = new ChangeInfo();
            changeInfo.setEventInfo(eventInfo);
            changeInfo.setIsChange(flag);
            changeInfo.setRedisKey(redisKey);
            changeInfo.setMapKey(mapKey);
            changeInfo.setCollectTime(new Date());
            info.getMaps().put(mapKey, changeInfo);

            String eventRedisKey = StatusInfoChangeTypeEnum.event_syslog_power.getCode();
            String eventMapKey = info.getAssetIp() + ":" + info.getAssetId() + ":syslog_power";

            AlarmTempReq tempReq = new AlarmTempReq();
            tempReq.setAssetIp(info.getAssetIp());
            tempReq.setOrgMsg(msg);
            tempReq.setFlag(eventMapKey);
            tempReq.setCollectValue(status + "");

            this.addEventStatus(StatusInfoChangeTypeEnum.event_syslog_power.getCode(), StatusInfoChangeTypeEnum.STATUS.getCode(),
                    eventMapKey, status, info, changeInfo);

            IEvent event = eventInfoChangeManagerService.creatChangeEvent(info.getAssetId(), changeInfo, eventRedisKey, eventMapKey, status, tempReq,info.getInspectRecordId());
            if (event != null) {
                //被事件信息截取
                changeInfo.setIsEvent(true);
                event.setDescLog(String.format(StatusInfoChangeTypeEnum.event_syslog_power.getDescr(), eventInfo.getMessage()));
                this.dispatureEvent(event);
                return false;
            }
        }
        return true;
    }

    private int isPower(String content) {
        Map<String, String> map = DictUtil.value("SYSLOG_POWER_KEYWORDS");
        Set<Map.Entry<String, String>> entries = map.entrySet();
        for (Map.Entry<String, String> entry : entries) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (value.contains("|")) {
                String[] split = value.split("\\|");
                if (content.contains(split[0]) && content.contains(split[1])) {
                    if (key.contains("lost")) {
                        return 1;
                    }
                    if (key.contains("gain")) {
                        return 2;
                    }
                }
            } else {
                if (content.contains(value)) {
                    if (key.contains("lost")) {
                        return 1;
                    }
                    if (key.contains("gain")) {
                        return 2;
                    }
                }
            }
        }
        return 0;
    }

    @Override
    public boolean isNeedNexthandle(Boolean flag) {
        return flag;
    }
}
