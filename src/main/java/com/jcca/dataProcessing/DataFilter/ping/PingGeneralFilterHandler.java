package com.jcca.dataProcessing.DataFilter.ping;

import com.jcca.dataProcessing.Entity.ChangeInfo;
import com.jcca.dataProcessing.Entity.ReceiveAlarmEntity;
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
 * @description TODO 普通单个设备ping
 * @className PingNoGroupFilterHandler
 * @date 2023/10/27 9:51
 * @since 2.1.0.0
 */
@Component("pingGeneralFilterHandler")
public class PingGeneralFilterHandler extends IFilterHandler<ReceiveAlarmEntity> {

    @Resource
    private IEventInfoManagerService eventInfoChangeManagerService;

    @Override
    public boolean handler(ReceiveAlarmEntity info) {
        String redisKey = info.getAssetIp() + ":" + info.getAssetId() + ":" + StatusInfoChangeTypeEnum.status.getCode();
        String mapKey = StatusInfoChangeTypeEnum.status_ping.getCode();

        boolean flag = eventInfoChangeManagerService.infoIschange(redisKey, mapKey, info.getFlag());
        if (flag) {
            ChangeInfo changeInfo = new ChangeInfo();
            changeInfo.setValue(info.getFlag());
            changeInfo.setRedisKey(redisKey);
            changeInfo.setMapKey(mapKey);
            changeInfo.setCollectTime(new Date());
            info.getMaps().put(mapKey, changeInfo);

            Integer status = info.getFlag() ? EventLevelEnum.NORMAL.getCode() : EventLevelEnum.ABNORMAL.getCode();
            String eventRedisKey = StatusInfoChangeTypeEnum.event_ping_no_group.getCode();
            String eventMapKey = info.getAssetIp() + "_" + info.getAssetId();
            this.addEventStatus(StatusInfoChangeTypeEnum.event_ping_no_group.getCode(),StatusInfoChangeTypeEnum.STATUS.getCode(),"", status, info, changeInfo);
            AlarmTempReq alarmTempReq = new AlarmTempReq();
            alarmTempReq.setOrgMsg(String.format(StatusInfoChangeTypeEnum.event_ping_no_group.getDescr()));
            alarmTempReq.setCollectValue(changeInfo.getValue().toString());
            IEvent event = eventInfoChangeManagerService.creatChangeEvent(info.getAssetId(), changeInfo, eventRedisKey, eventMapKey, status,alarmTempReq);
            if (event != null) {
                //被事件信息截取
                changeInfo.setIsEvent(true);
                event.setDescStr(String.format(StatusInfoChangeTypeEnum.event_ping_no_group.getDescr()));
                this.dispatureEvent(event);
            }
        }

        return true;
    }

    @Override
    public boolean isNeedNexthandle(Boolean flag) {
        return flag;
    }


}
