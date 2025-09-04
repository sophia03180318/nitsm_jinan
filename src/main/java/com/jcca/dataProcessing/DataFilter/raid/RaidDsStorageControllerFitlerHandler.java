package com.jcca.dataProcessing.DataFilter.raid;

import com.jcca.dataProcessing.Entity.ChangeInfo;
import com.jcca.dataProcessing.Entity.DSEntity;
import com.jcca.dataProcessing.enums.StatusInfoChangeTypeEnum;
import com.jcca.dataProcessing.manager.IEventInfoManagerService;
import com.jcca.dataProcessing.support.IFilterHandler;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;

/**
 * @author Zhaozheng
 * @description TODO Raid存储 Vdisk过滤处理类
 * @className StorageVidskFitlerHandler
 * @date 2023/10/27 9:35
 * @since 2.1.0.0
 */
@Component("raidDsStorageControllerFitlerHandler")
public class RaidDsStorageControllerFitlerHandler extends IFilterHandler<DSEntity> {

    @Resource
    private IEventInfoManagerService eventInfoChangeManagerService;

    @Override
    public boolean handler(DSEntity info) {
        if (info.getType() == 0) {
            String redisKey = info.getAssetIp() + ":" + info.getAssetId() + ":" + StatusInfoChangeTypeEnum.status_raidDS_controller.getCode() + ":" + info.getName();
            String mapKey1 = info.getName();
            boolean flag1 = eventInfoChangeManagerService.infoIschange(info.getInspectRecordId(),redisKey, mapKey1, info.getName());
            if (flag1) {
                ChangeInfo changeInfo = this.createChangeInfo(info.getName(), redisKey, mapKey1);
                info.getMaps().put(mapKey1, changeInfo);
            }
       /*     String mapKey3 = StatusInfoChangeTypeEnum.status_raid_diskType.getCode();
            boolean flag3 = eventInfoChangeManagerService.infoIschange(redisKey, mapKey3, info.getType());
            if (flag3) {
                ChangeInfo changeInfo = this.createChangeInfo(info.getType(), redisKey, mapKey3);
                info.getMaps().put(mapKey3, changeInfo);
            }
            String mapKey4 = StatusInfoChangeTypeEnum.status_raidDS_controller_status.getCode();
            boolean flag4 = eventInfoChangeManagerService.infoIschange(redisKey, mapKey4, info.getStatusInfo());
            if (flag4) {
                ChangeInfo changeInfo = this.createChangeInfo(info.getStatusInfo(), redisKey, mapKey4);

                info.getMaps().put(mapKey4, changeInfo);
                Integer status = info.getStatusInfo().toLowerCase().equals("online") ? EventLevelEnum.NORMAL.getCode() : EventLevelEnum.ABNORMAL.getCode();
                String eventRedisKey = StatusInfoChangeTypeEnum.event_storage_controller.getCode();
                String eventMapKey = info.getAssetIp() + "_" + info.getAssetId() + "_" + info.getName();
                String str = status == EventLevelEnum.NORMAL.getCode() ? "恢复" : "异常";
                AlarmTempReq alarmTempReq = new AlarmTempReq();
                alarmTempReq.setOrgMsg(String.format(StatusInfoChangeTypeEnum.event_storage_controller.getDescr(), info.getName(), str));
                alarmTempReq.setCollectValue(info.getStatusInfo());
                alarmTempReq.setFlag(info.getName());
                this.addEventStatus(StatusInfoChangeTypeEnum.event_storage_controller.getCode(),StatusInfoChangeTypeEnum.STATUS.getCode(),info.getName(), status, info, changeInfo);
                IEvent event = eventInfoChangeManagerService.creatChangeEvent(info.getAssetId(), changeInfo, eventRedisKey, eventMapKey, status,alarmTempReq);
                if (event != null) {
                    //被事件信息截取

                    event.setDescStr(String.format(StatusInfoChangeTypeEnum.event_storage_controller.getDescr(), info.getName(), str));
                    changeInfo.setIsEvent(true);
                    this.dispatureEvent(event);
                }
            }

            String mapKey6 = StatusInfoChangeTypeEnum.status_raid_raidLevel.getCode();
            boolean flag6 = eventInfoChangeManagerService.infoIschange(redisKey, mapKey6, info.getRaidLevel());
            if(flag6){
                ChangeInfo changeInfo= this.createChangeInfo(info.getRaidLevel(),redisKey,mapKey6);
                info.getMaps().put(mapKey6,changeInfo);
            }

*/
        }
        return true;
    }

    @Override
    public boolean isNeedNextHandle(Boolean flag) {
        return flag;
    }

    private ChangeInfo createChangeInfo(Object value, String redisKey, String mapKey){
        ChangeInfo changeInfo=new ChangeInfo();
        changeInfo.setValue(value);
        changeInfo.setRedisKey(redisKey);
        changeInfo.setCollectTime(new Date());
        changeInfo.setMapKey(mapKey);
        return changeInfo;
    }


}
