package com.jcca.dataProcessing.DataFilter.raid;

import com.jcca.dataProcessing.Entity.ChangeInfo;
import com.jcca.dataProcessing.Entity.RaidCommonLogEntity;
import com.jcca.dataProcessing.enums.StatusInfoChangeTypeEnum;
import com.jcca.dataProcessing.manager.IEventInfoManagerService;
import com.jcca.dataProcessing.support.IFilterHandler;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;

/**
 * @author Zhaozheng
 * @description TODO Raid存储 group信息过滤处理类
 * @className StorageGroupFilterHandler
 * @date 2023/10/27 9:34
 * @since 2.1.0.0
 */
@Component("raidCommonStorageLogFilterHandler")
public class RaidCommonStorageLogFilterHandler extends IFilterHandler<RaidCommonLogEntity> {

    @Resource
    private IEventInfoManagerService eventInfoChangeManagerService;

    @Override
    public boolean handler(RaidCommonLogEntity info) {

        String redisKey = info.getAssetIp() + ":" + info.getAssetId() + ":" + StatusInfoChangeTypeEnum.status_raid_log.getCode();

        String mapKey3 = info.getLog();
        boolean flag3 = eventInfoChangeManagerService.infoIschange(info.getInspectRecordId(),redisKey, mapKey3, info.getLog());
        if (flag3) {
            ChangeInfo changeInfo = this.createChangeInfo(info.getLog(), redisKey, mapKey3);
            info.getMaps().put(mapKey3, changeInfo);
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
