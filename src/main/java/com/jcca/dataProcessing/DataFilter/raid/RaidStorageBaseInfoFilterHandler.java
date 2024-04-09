package com.jcca.dataProcessing.DataFilter.raid;

import cn.hutool.core.util.StrUtil;
import com.jcca.common.utils.AppMathUtil;
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
 * @description TODO Raid存储 容量信息过滤处理类
 * @className StorageCapacityFilterHandler
 * @date 2023/10/27 9:35
 * @since 2.1.0.0
 */
@Component("raidStorageBaseInfoFilterHandler")
public class RaidStorageBaseInfoFilterHandler extends IFilterHandler<DiskEntity> {

    @Resource
    private IEventInfoManagerService eventInfoChangeManagerService;

    @Override
    public boolean handler(DiskEntity info) {
        if (info.getDiskType() == 3) {
            String totalCapacity = info.getCapacity();
            String usedCapacity = info.getUsedCapacity();
            Long freeCapacity = 0l;

            String redisKey = info.getAssetIp() + ":" + info.getAssetId() + ":" + StatusInfoChangeTypeEnum.status.getCode();

            String mapKey1 = StatusInfoChangeTypeEnum.status_raid_totalCapacity.getCode();
            String mapKey2 = StatusInfoChangeTypeEnum.status_raid_usedCapacity.getCode();
            String mapKey3 = StatusInfoChangeTypeEnum.status_raid_freeCapacity.getCode();

            if(StrUtil.isNotEmpty(totalCapacity) && StrUtil.isNotEmpty(usedCapacity)){
                freeCapacity = AppMathUtil.sub(totalCapacity, usedCapacity);
            }

            boolean flag = eventInfoChangeManagerService.infoIschange(redisKey, mapKey1, totalCapacity);
            boolean flag2 = eventInfoChangeManagerService.infoIschange(redisKey, mapKey2, usedCapacity);
            boolean flag3 = eventInfoChangeManagerService.infoIschange(redisKey, mapKey3, freeCapacity);
            if (flag) {
                ChangeInfo changeInfo = new ChangeInfo();
                changeInfo.setValue(totalCapacity);
                changeInfo.setRedisKey(redisKey);
                changeInfo.setMapKey(mapKey1);
                changeInfo.setCollectTime(new Date());
                info.getMaps().put(mapKey1, changeInfo);
            }
            if (flag2) {
                ChangeInfo changeInfo = new ChangeInfo();
                changeInfo.setValue(usedCapacity);
                changeInfo.setRedisKey(redisKey);
                changeInfo.setCollectTime(new Date());
                changeInfo.setMapKey(mapKey2);
                info.getMaps().put(mapKey2, changeInfo);
            }
            if (flag3) {
                ChangeInfo changeInfo = new ChangeInfo();
                changeInfo.setValue(freeCapacity);
                changeInfo.setRedisKey(redisKey);
                changeInfo.setCollectTime(new Date());
                changeInfo.setMapKey(mapKey3);
                info.getMaps().put(mapKey3, changeInfo);
            }
        }
        return true;
    }

    @Override
    public boolean isNeedNexthandle(Boolean flag) {
        return flag;
    }


}
