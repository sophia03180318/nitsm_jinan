package com.jcca.dataProcessing.DataFilter.raid;

import com.jcca.dataProcessing.Entity.ChangeInfo;
import com.jcca.dataProcessing.Entity.DiskEntity;
import com.jcca.dataProcessing.enums.StatusInfoChangeTypeEnum;
import com.jcca.dataProcessing.manager.IEventInfoManagerService;
import com.jcca.dataProcessing.manager.bean.AlarmTempReq;
import com.jcca.dataProcessing.support.IEvent;
import com.jcca.dataProcessing.support.IFilterHandler;
import com.jcca.web.event.enums.EventLevelEnum;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author Zhaozheng
 * @description TODO
 * @className RaidStorageStateFilterHandler
 * @date 2024/1/6 20:19
 * @since 2.1.0.0
 */
@Component("raidStorageStateFilterHandler")
public class RaidStorageStateFilterHandler extends IFilterHandler<DiskEntity> {
    @Resource
    private IEventInfoManagerService eventInfoChangeManagerService;

    @Override
    public boolean handler(DiskEntity info) {

        ChangeInfo changeInfo = info.getMaps().get(StatusInfoChangeTypeEnum.status_raid_status.getCode());
        if (changeInfo == null || changeInfo.getValue() == null) {
            return true;
        }
        Integer status =  EventLevelEnum.ABNORMAL.getCode();
        if(changeInfo.getValue().toString().toLowerCase().equals("online")||changeInfo.getValue().toString().toLowerCase().equals("正常")){
            status = EventLevelEnum.NORMAL.getCode();
        }
        String eventRedisKey = null;
        String message = "";
        String str = status == EventLevelEnum.NORMAL.getCode() ? "恢复" : "异常";
        String key[] = changeInfo.getRedisKey().split(":");
        String keystr = key[key.length - 1];
        if (info.getDiskType() == 4) {
            //Driver
            eventRedisKey = StatusInfoChangeTypeEnum.event_storage_driver.getCode();
            message = String.format(StatusInfoChangeTypeEnum.event_storage_driver.getDescr(), keystr, str);
        }  else if (info.getDiskType() == 1) {
            //mdisk
            eventRedisKey = StatusInfoChangeTypeEnum.event_storage_Mdisk.getCode();
            message = String.format(StatusInfoChangeTypeEnum.event_storage_group.getDescr(), keystr, str);

        } else if (info.getDiskType() == 2) {
            //vdisk
            eventRedisKey = StatusInfoChangeTypeEnum.event_storage_Vdisk.getCode();
            message = String.format(StatusInfoChangeTypeEnum.event_storage_Vdisk.getDescr(), keystr, str);
        }
        AlarmTempReq alarmTempReq = new AlarmTempReq();
        alarmTempReq.setOrgMsg(message);
        alarmTempReq.setCollectValue(changeInfo.getValue().toString());
        alarmTempReq.setFlag(keystr);
        String eventMapKey = info.getAssetIp() + "_" + info.getAssetId() + "_" + keystr;
        IEvent event = eventInfoChangeManagerService.creatChangeEvent(info.getAssetId(), changeInfo, eventRedisKey, eventMapKey, status,alarmTempReq,info.getInspectRecordId(),info.getVersion());
        if (event != null) {
            //被事件信息截取
            event.setDescStr(message);
            changeInfo.setIsEvent(true);
            this.dispatureEvent(event);
        }

        return true;
    }

    @Override
    public boolean isNeedNextHandle(Boolean flag) {
        return flag;
    }
}
