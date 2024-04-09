package com.jcca.dataProcessing.DataFilter.raid;

import com.jcca.dataProcessing.Entity.ChangeInfo;
import com.jcca.dataProcessing.Entity.DSEntity;
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
 * @description TODO Raid存储 Vdisk过滤处理类
 * @className StorageVidskFitlerHandler
 * @date 2023/10/27 9:35
 * @since 2.1.0.0
 */
@Component("raidDsStorageArrayFitlerHandler")
public class RaidDsStorageArrayFitlerHandler extends IFilterHandler<DSEntity> {

    @Resource
    private IEventInfoManagerService eventInfoChangeManagerService;

    @Override
    public boolean handler(DSEntity info) {
        if (info.getType() == 1) {
            String redisKey = info.getAssetIp() + ":" + info.getAssetId() + ":" + StatusInfoChangeTypeEnum.status_raid_mdisk.getCode() + ":" + info.getName();
            String mapKey1 = info.getName();
            boolean flag1 = eventInfoChangeManagerService.infoIschange(redisKey, mapKey1, info.getName());
            if (flag1) {
                ChangeInfo changeInfo = this.createChangeInfo(info.getName(), redisKey, mapKey1);
                info.getMaps().put(mapKey1, changeInfo);
            }
            String mapKey3 = StatusInfoChangeTypeEnum.status_raid_diskType.getCode();
            boolean flag3 = eventInfoChangeManagerService.infoIschange(redisKey, mapKey3, info.getType());
            if (flag3) {
                ChangeInfo changeInfo = this.createChangeInfo(info.getType(), redisKey, mapKey3);
                info.getMaps().put(mapKey3, changeInfo);
            }
            String mapKey4 = StatusInfoChangeTypeEnum.status_raidDS_mdisk_status.getCode();
            boolean flag4 = eventInfoChangeManagerService.infoIschange(redisKey, mapKey4, info.getStatusInfo());
            if (flag4) {
                ChangeInfo changeInfo = this.createChangeInfo(info.getStatusInfo(), redisKey, mapKey4);
                info.getMaps().put(mapKey4, changeInfo);
                Integer status = info.getStatusInfo().toLowerCase().equals("optimal") ? EventLevelEnum.NORMAL.getCode() : EventLevelEnum.ABNORMAL.getCode();
                String eventRedisKey = StatusInfoChangeTypeEnum.event_storage_Mdisk.getCode();
                String eventMapKey = info.getAssetIp() + "_" + info.getAssetId() + "_" + info.getName();
                String str = status == EventLevelEnum.NORMAL.getCode() ? "恢复" : "异常";
                AlarmTempReq alarmTempReq = new AlarmTempReq();
                alarmTempReq.setOrgMsg(String.format(StatusInfoChangeTypeEnum.event_storage_group.getDescr(), info.getName(), str));
                alarmTempReq.setCollectValue(changeInfo.getValue().toString());
                alarmTempReq.setFlag(info.getName());
                IEvent event = eventInfoChangeManagerService.creatChangeEvent(info.getAssetId(), changeInfo, eventRedisKey, eventMapKey, status,alarmTempReq);
                if (event != null) {
                    //被事件信息截取
                    changeInfo.setIsEvent(true);
                    event.setDescStr(String.format(StatusInfoChangeTypeEnum.event_storage_group.getDescr(), info.getName(), str));
                    this.dispatureEvent(event);
                }
            }

            String mapKey6 = StatusInfoChangeTypeEnum.status_raid_raidLevel.getCode();
            boolean flag6 = eventInfoChangeManagerService.infoIschange(redisKey, mapKey6, info.getRaidLevel());
            if (flag6) {
                ChangeInfo changeInfo = this.createChangeInfo(info.getRaidLevel(), redisKey, mapKey6);
                info.getMaps().put(mapKey6, changeInfo);
            }
            String mapKey7 = StatusInfoChangeTypeEnum.status_raid_capacity.getCode();
            boolean flag7 = eventInfoChangeManagerService.infoIschange(redisKey, mapKey7, info.getCapacity());
            if (flag7) {
                ChangeInfo changeInfo = this.createChangeInfo(info.getCapacity(), redisKey, mapKey7);
                info.getMaps().put(mapKey7, changeInfo);
            }
            String mapKey8 = StatusInfoChangeTypeEnum.status_raid_freeCapacity.getCode();
            boolean flag8 = eventInfoChangeManagerService.infoIschange(redisKey, mapKey8, info.getFreeCapacity());
            if (flag8) {
                ChangeInfo changeInfo = this.createChangeInfo(info.getFreeCapacity(), redisKey, mapKey8);
                info.getMaps().put(mapKey8, changeInfo);
            }
            String mapKey9 = StatusInfoChangeTypeEnum.status_raid_parentOrgName.getCode();
            boolean flag9 = eventInfoChangeManagerService.infoIschange(redisKey, mapKey9, info.getParentOrgName());
            if (flag9) {
                ChangeInfo changeInfo = this.createChangeInfo(info.getParentOrgName(), redisKey, mapKey9);
                info.getMaps().put(mapKey9, changeInfo);
            }

            String mapKey13 = StatusInfoChangeTypeEnum.status_raid_capacityStr.getCode();
            boolean flag13 = eventInfoChangeManagerService.infoIschange(redisKey, mapKey13, info.getCapacityStr());
            if (flag13) {
                ChangeInfo changeInfo = this.createChangeInfo(info.getCapacityStr(), redisKey, mapKey13);
                info.getMaps().put(mapKey13, changeInfo);
            }
            String mapKey14 = StatusInfoChangeTypeEnum.status_raid_freeCapacityStr.getCode();
            boolean flag14 = eventInfoChangeManagerService.infoIschange(redisKey, mapKey14, info.getFreeCapacityStr());
            if (flag14) {
                ChangeInfo changeInfo = this.createChangeInfo(info.getFreeCapacityStr(), redisKey, mapKey14);
                info.getMaps().put(mapKey13, changeInfo);
            }


        }
        return true;
    }

    @Override
    public boolean isNeedNexthandle(Boolean flag) {
        return flag;
    }

    private ChangeInfo createChangeInfo(Object value, String redisKey, String mapKey) {
        ChangeInfo changeInfo = new ChangeInfo();
        changeInfo.setValue(value);
        changeInfo.setRedisKey(redisKey);
        changeInfo.setCollectTime(new Date());
        changeInfo.setMapKey(mapKey);
        return changeInfo;
    }


}
