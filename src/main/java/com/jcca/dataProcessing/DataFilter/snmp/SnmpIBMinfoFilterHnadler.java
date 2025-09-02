package com.jcca.dataProcessing.DataFilter.snmp;

import cn.hutool.log.Log;
import com.jcca.dataProcessing.Entity.ChangeInfo;
import com.jcca.dataProcessing.Entity.EventInfo;
import com.jcca.dataProcessing.Entity.SnmpEventInfoEntity;
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
 * @author Zhaozheng
 * @description TODO
 * @className SnmpIBMinfoFilterHnadler
 * @date 2023/11/28 11:59
 * @since 2.1.0.0
 */

/**
 * IBM设备snmp告警信息
 * .1.3.6.1.4.1.2.6.158.5.1.1   "Timestamp of Local Date and Time when alert was generated"
 * .1.3.6.1.4.1.2.6.158.5.1.3     "SP System Identification - Text Identification"
 * .1.3.6.1.4.1.2.6.158.5.1.5     "Host System UUID(Universal Unique ID)"
 * .1.3.6.1.4.1.2.6.158.5.1.6      "Host System Serial Number"
 * .1.3.6.1.4.1.2.6.158.5.1.8      "Alert Severity Value - Critical Alert(0)  - Non-Critical Alert(2) - System Alert(4)- Recovery(8)"
 * .1.3.6.1.4.1.2.6.158.5.1.9        "Alert Message Text"
 * .1.3.6.1.4.1.2.6.158.5.1.10        "Alert Message ID"
 * .1.3.6.1.4.1.2.6.158.5.1.11        "Alert Message ID"
 * .1.3.6.1.4.1.2.6.158.5.1.12        "Host Contact"
 * .1.3.6.1.4.1.2.6.158.5.1.13        "Host Location"
 */
@Component("snmpIBMinfoFilterHnadler")
public class SnmpIBMinfoFilterHnadler extends IFilterHandler<SnmpEventInfoEntity> {

    @Resource
    private IEventInfoManagerService eventInfoChangeManagerService;

    @Override
    public boolean handler(SnmpEventInfoEntity info) {
        if (info.getMap().containsKey("1.3.6.1.4.1.2.6.158.5.1.8")) {
            String level = info.getMap().get("1.3.6.1.4.1.2.6.158.5.1.8");
            String text = info.getMap().get("1.3.6.1.4.1.2.6.158.5.1.9");
            String messageId = info.getMap().get("1.3.6.1.4.1.2.6.158.5.1.10");

            String msg = String.format(StatusInfoChangeTypeEnum.event_snmp.getDescr(), text);

            EventInfo eventInfo = new EventInfo();
            eventInfo.setLevel(Integer.parseInt(level));
            eventInfo.setMessageId(messageId);
            eventInfo.setMessage(msg);

            String eventRedisKey = StatusInfoChangeTypeEnum.event_snmp.getCode();
            String eventMapKey = info.getAssetIp() + "_" + info.getAssetId();

            ChangeInfo changeInfo = new ChangeInfo();
            changeInfo.setEventInfo(eventInfo);
            changeInfo.setCollectTime(new Date());
            changeInfo.setRedisKey(eventRedisKey);
            changeInfo.setMapKey(eventMapKey);


            AlarmTempReq tempReq = new AlarmTempReq();
            tempReq.setOrgMsg(msg);
            tempReq.setAssetIp(info.getAssetIp());

            IEvent event = eventInfoChangeManagerService.creatChangeEvent(info.getAssetId(), changeInfo, eventRedisKey, eventMapKey, EventLevelEnum.ABNORMAL.getCode(),tempReq,info.getInspectRecordId());
            if (event != null) {
                event.setDescStr(msg);
                changeInfo.setIsEvent(true);
            }

            this.dispatureEvent(event);
            return false;
        }

        return true;

    }

    @Override
    public boolean isNeedNextHandle(Boolean flag) {
        return flag;
    }


}
