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
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @author Zhaozheng
 * @description TODO Raid存储 group信息过滤处理类
 * @className StorageGroupFilterHandler
 * @date 2023/10/27 9:34
 * @since 2.1.0.0
 */
@Component("raidDsStorageLogicDriverFilterHandler")
public class RaidDsStorageLogicDriverFilterHandler extends IFilterHandler<DSEntity> {

    @Resource
    private IEventInfoManagerService eventInfoChangeManagerService;

    @Override
    public boolean handler(DSEntity info) {
        if (info.getType() == 2) {
            String redisKey = info.getAssetIp() + ":" + info.getAssetId() + ":" + StatusInfoChangeTypeEnum.status_raidDS_logicDrives.getCode() + ":" + info.getParentOrgName() + ":" + info.getName();
            ;
            String mapKey1 = StatusInfoChangeTypeEnum.status_raid_name.getCode();
            boolean flag1 = eventInfoChangeManagerService.infoIschange(info.getInspectRecordId(),redisKey, mapKey1, info.getName());
            if (flag1) {
                ChangeInfo changeInfo = this.createChangeInfo(info.getName(), redisKey, mapKey1);
                info.getMaps().put(mapKey1, changeInfo);
            }
            String mapKey3 = StatusInfoChangeTypeEnum.status_raid_diskType.getCode();
            boolean flag3 = eventInfoChangeManagerService.infoIschange(info.getInspectRecordId(),redisKey, mapKey3, info.getType());
            if (flag3) {
                ChangeInfo changeInfo = this.createChangeInfo(info.getType(), redisKey, mapKey3);
                info.getMaps().put(mapKey3, changeInfo);
            }
            String mapKey4 = StatusInfoChangeTypeEnum.status_raid_driver_status.getCode();
            boolean flag4 = eventInfoChangeManagerService.infoIschange(info.getInspectRecordId(),redisKey, mapKey4, info.getStatusInfo());
            if (flag4) {
                ChangeInfo changeInfo = this.createChangeInfo(info.getStatusInfo(), redisKey, mapKey4);

                info.getMaps().put(mapKey4, changeInfo);
                Integer status = EventLevelEnum.ABNORMAL.getCode();
                List<String> matchList = Arrays.asList("正常", "optimal");
                if(matchList.contains(info.getStatusInfo().toLowerCase())){
                    status = EventLevelEnum.NORMAL.getCode();
                }

                String eventRedisKey = StatusInfoChangeTypeEnum.event_storage_logic_driver.getCode();
                String eventMapKey = info.getAssetIp() + "_" + info.getAssetId() + "_" + info.getParentOrgName() + "_" + info.getName();
                String str = EventLevelEnum.NORMAL.getCode().equals(status) ? "恢复" : "异常";
                AlarmTempReq alarmTempReq = new AlarmTempReq();
                alarmTempReq.setOrgMsg(String.format(StatusInfoChangeTypeEnum.event_storage_logic_driver.getDescr(), info.getParentOrgName() + " " + info.getName(), str));
                alarmTempReq.setCollectValue(info.getStatusInfo());
                alarmTempReq.setFlag(info.getName());
                IEvent event = eventInfoChangeManagerService.creatChangeEvent(info.getAssetId(), changeInfo, eventRedisKey, eventMapKey, status,alarmTempReq);
                if (event != null) {
                    //被事件信息截取
                    event.setDescStr(String.format(StatusInfoChangeTypeEnum.event_storage_logic_driver.getDescr(), info.getParentOrgName() + " " + info.getName(), str));
                    changeInfo.setIsEvent(true);
                    this.dispatureEvent(event);
                }
            }

            String mapKey6 = StatusInfoChangeTypeEnum.status_raid_raidLevel.getCode();
            boolean flag6 = eventInfoChangeManagerService.infoIschange(info.getInspectRecordId(),redisKey, mapKey6, info.getRaidLevel());
            if (flag6) {
                ChangeInfo changeInfo = this.createChangeInfo(info.getRaidLevel(), redisKey, mapKey6);
                info.getMaps().put(mapKey6, changeInfo);
            }
            String mapKey7 = StatusInfoChangeTypeEnum.status_raid_capacity.getCode();
            boolean flag7 = eventInfoChangeManagerService.infoIschange(info.getInspectRecordId(),redisKey, mapKey7, info.getCapacity());
            if (flag7) {
                ChangeInfo changeInfo = this.createChangeInfo(info.getCapacity(), redisKey, mapKey7);
                info.getMaps().put(mapKey7, changeInfo);
            }
            String mapKey8 = StatusInfoChangeTypeEnum.status_raid_freeCapacity.getCode();
            boolean flag8 = eventInfoChangeManagerService.infoIschange(info.getInspectRecordId(),redisKey, mapKey8, info.getFreeCapacity());
            if (flag8) {
                ChangeInfo changeInfo = this.createChangeInfo(info.getFreeCapacity(), redisKey, mapKey8);
                info.getMaps().put(mapKey8, changeInfo);
            }
            String mapKey9 = StatusInfoChangeTypeEnum.status_raid_parentOrgName.getCode();
            boolean flag9 = eventInfoChangeManagerService.infoIschange(info.getInspectRecordId(),redisKey, mapKey9, info.getParentOrgName());
            if (flag9) {
                ChangeInfo changeInfo = this.createChangeInfo(info.getParentOrgName(), redisKey, mapKey9);
                info.getMaps().put(mapKey9, changeInfo);
            }
            String mapKey10 = StatusInfoChangeTypeEnum.status_raid_parentOrgId.getCode();
            boolean flag10 = eventInfoChangeManagerService.infoIschange(info.getInspectRecordId(),redisKey, mapKey10, info.getParentOrgId());
            if (flag10) {
                ChangeInfo changeInfo = this.createChangeInfo(info.getParentOrgId(), redisKey, mapKey10);
                info.getMaps().put(mapKey10, changeInfo);
            }

            String mapKey13 = StatusInfoChangeTypeEnum.status_raid_capacityStr.getCode();
            boolean flag13 = eventInfoChangeManagerService.infoIschange(info.getInspectRecordId(),redisKey, mapKey13, info.getCapacityStr());
            if (flag13) {
                ChangeInfo changeInfo = this.createChangeInfo(info.getCapacityStr(), redisKey, mapKey13);
                info.getMaps().put(mapKey13, changeInfo);
            }
            String mapKey14 = StatusInfoChangeTypeEnum.status_raid_freeCapacityStr.getCode();
            boolean flag14 = eventInfoChangeManagerService.infoIschange(info.getInspectRecordId(),redisKey, mapKey14, info.getFreeCapacityStr());
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

    private ChangeInfo createChangeInfo(Object value, String redisKey, String mapKey){
        ChangeInfo changeInfo=new ChangeInfo();
        changeInfo.setValue(value);
        changeInfo.setRedisKey(redisKey);
        changeInfo.setCollectTime(new Date());
        changeInfo.setMapKey(mapKey);
        return changeInfo;
    }


}
