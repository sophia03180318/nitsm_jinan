package com.jcca.dataProcessing.DataFilter.raid;

import com.jcca.dataProcessing.Entity.ChangeInfo;
import com.jcca.dataProcessing.Entity.DiskEntity;
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
@Component("raidStorageVidskFitlerHandler")
public class RaidStorageVidskFitlerHandler extends IFilterHandler<DiskEntity> {

    @Resource
    private IEventInfoManagerService eventInfoChangeManagerService;

    @Override
    public boolean handler(DiskEntity info) {
        if (info.getDiskType() == 2) {
            String redisKey = info.getAssetIp() + ":" + info.getAssetId() + ":" + StatusInfoChangeTypeEnum.status_raid_vdisk.getCode();
            String mapKey1 = StatusInfoChangeTypeEnum.status_raid_name.getCode();
            boolean flag1 = eventInfoChangeManagerService.infoIschange(redisKey, mapKey1, info.getName());
            if (flag1) {
                ChangeInfo changeInfo = this.createChangeInfo(info.getName(), redisKey, mapKey1);
                info.getMaps().put(mapKey1, changeInfo);
            }
            String mapKey2 = StatusInfoChangeTypeEnum.status_raid_realid.getCode();
            boolean flag2 = eventInfoChangeManagerService.infoIschange(redisKey, mapKey2, info.getRealId());
            if (flag2) {
                ChangeInfo changeInfo = this.createChangeInfo(info.getRealId(), redisKey, mapKey2);
                info.getMaps().put(mapKey2, changeInfo);
            }
            String mapKey3 = StatusInfoChangeTypeEnum.status_raid_diskType.getCode();
            boolean flag3 = eventInfoChangeManagerService.infoIschange(redisKey, mapKey3, info.getDiskType());
            if (flag3) {
                ChangeInfo changeInfo = this.createChangeInfo(info.getDiskType(), redisKey, mapKey3);
                info.getMaps().put(mapKey3, changeInfo);
            }
            String mapKey4 = StatusInfoChangeTypeEnum.status_raid_status.getCode();
            boolean flag4 = eventInfoChangeManagerService.infoIschange(redisKey, mapKey4, info.getStatus());
            if (flag4) {
                ChangeInfo changeInfo = this.createChangeInfo(info.getStatus(), redisKey, mapKey4);
                info.getMaps().put(mapKey4, changeInfo);
            }
            String mapKey5 = StatusInfoChangeTypeEnum.status_raid_grpid.getCode();
            boolean flag5 = eventInfoChangeManagerService.infoIschange(redisKey, mapKey5, info.getGrpId());
            if (flag5) {
                ChangeInfo changeInfo = this.createChangeInfo(info.getGrpId(), redisKey, mapKey5);
                info.getMaps().put(mapKey5, changeInfo);
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
            String mapKey8 = StatusInfoChangeTypeEnum.status_raid_usedCapacity.getCode();
            boolean flag8 = eventInfoChangeManagerService.infoIschange(redisKey, mapKey8, info.getUsedCapacity());
            if (flag8) {
                ChangeInfo changeInfo = this.createChangeInfo(info.getUsedCapacity(), redisKey, mapKey8);
                info.getMaps().put(mapKey8, changeInfo);
            }
            String mapKey9 = StatusInfoChangeTypeEnum.status_raid_parentOrgName.getCode();
            boolean flag9 = eventInfoChangeManagerService.infoIschange(redisKey, mapKey9, info.getParentOrgName());
            if (flag9) {
                ChangeInfo changeInfo = this.createChangeInfo(info.getParentOrgName(), redisKey, mapKey9);
                info.getMaps().put(mapKey9, changeInfo);
            }
            String mapKey10 = StatusInfoChangeTypeEnum.status_raid_parentOrgId.getCode();
            boolean flag10 = eventInfoChangeManagerService.infoIschange(redisKey, mapKey10, info.getParentOrgId());
            if (flag10) {
                ChangeInfo changeInfo = this.createChangeInfo(info.getParentOrgId(), redisKey, mapKey10);
                info.getMaps().put(mapKey10, changeInfo);
            }
            String mapKey11 = StatusInfoChangeTypeEnum.status_raid_xindx.getCode();
            boolean flag11 = eventInfoChangeManagerService.infoIschange(redisKey, mapKey11, info.getXindex());
            if (flag11) {
                ChangeInfo changeInfo = this.createChangeInfo(info.getXindex(), redisKey, mapKey11);
                info.getMaps().put(mapKey11, changeInfo);
            }
            String mapKey12 = StatusInfoChangeTypeEnum.status_raid_yindx.getCode();
            boolean flag12 = eventInfoChangeManagerService.infoIschange(redisKey, mapKey12, info.getYindex());
            if (flag12) {
                ChangeInfo changeInfo = this.createChangeInfo(info.getYindex(), redisKey, mapKey12);
                info.getMaps().put(mapKey12, changeInfo);
            }
            String mapKey13 = StatusInfoChangeTypeEnum.status_raid_capacityStr.getCode();
            boolean flag13 = eventInfoChangeManagerService.infoIschange(redisKey, mapKey13, info.getCapacityStr());
            if (flag13) {
                ChangeInfo changeInfo = this.createChangeInfo(info.getCapacityStr(), redisKey, mapKey13);
                info.getMaps().put(mapKey13, changeInfo);
            }
            String mapKey14 = StatusInfoChangeTypeEnum.status_raid_usedCapacityStr.getCode();
            boolean flag14 = eventInfoChangeManagerService.infoIschange(redisKey, mapKey14, info.getUsedCapacityStr());
            if (flag14) {
                ChangeInfo changeInfo = this.createChangeInfo(info.getUsedCapacityStr(), redisKey, mapKey14);
                info.getMaps().put(mapKey13, changeInfo);
            }


        }

        return true;
    }

    @Override
    public boolean isNeedNexthandle(Boolean flag) {
        return true;
    }

    private ChangeInfo createChangeInfo(Object value, String redisKey, String mapKey){
        ChangeInfo changeInfo=new ChangeInfo();
        changeInfo.setValue(value);
        changeInfo.setCollectTime(new Date());
        changeInfo.setRedisKey(redisKey);
        changeInfo.setMapKey(mapKey);
        return changeInfo;
    }


}
