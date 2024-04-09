package com.jcca.dataProcessing.DataFilter.DB;

import com.jcca.dataProcessing.Entity.ChangeInfo;
import com.jcca.dataProcessing.Entity.CollectTablespaceEntity;
import com.jcca.dataProcessing.enums.StatusInfoChangeTypeEnum;
import com.jcca.dataProcessing.manager.IEventInfoManagerService;
import com.jcca.dataProcessing.support.IFilterHandler;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;

/**
 * @author Zhaozheng
 * @description TODO 数据库表空间阈值过滤处理类
 * @className DBTableSpaceFilterHandler
 * @date 2023/10/27 9:30
 * @since 2.1.0.0
 */
@Component("dbTableSpaceInfoFilterHandler")
public class DbTableSpaceInfoFilterHandler extends IFilterHandler<CollectTablespaceEntity> {

    @Resource
    private IEventInfoManagerService eventInfoChangeManagerService;

    @Override
    public boolean handler(CollectTablespaceEntity info) {
        String redisKey = info.getAssetIp() + ":" + info.getAssetId() + ":" + StatusInfoChangeTypeEnum.status_tablespace.getCode() + ":" + info.getName();
        String mapKey1 = StatusInfoChangeTypeEnum.status_tablespace_totalSize.getCode();
        boolean flag1 = eventInfoChangeManagerService.infoIschange(redisKey, mapKey1, info.getTotalSize());
        if (flag1) {
            ChangeInfo changeInfo = new ChangeInfo();
            changeInfo.setValue(info.getTotalSize());
            changeInfo.setCollectTime(new Date(info.getCollectTime()));
            changeInfo.setRedisKey(redisKey);
            changeInfo.setMapKey(mapKey1);
            info.getMaps().put(mapKey1 + "_" + info.getName(), changeInfo);
        }
        String mapKey2 = StatusInfoChangeTypeEnum.status_tablespace_freeSize.getCode();
        boolean flag2 = eventInfoChangeManagerService.infoIschange(redisKey, mapKey2, info.getFreeSize());
        if (flag2) {
            ChangeInfo changeInfo = new ChangeInfo();
            changeInfo.setValue(info.getTotalSize());
            changeInfo.setCollectTime(new Date(info.getCollectTime()));
            changeInfo.setRedisKey(redisKey);
            changeInfo.setMapKey(mapKey2);
            info.getMaps().put(mapKey2 + "_" + info.getName(), changeInfo);
        }
        String mapKey3 = StatusInfoChangeTypeEnum.status_tablespace_usedSize.getCode();
        boolean flag3 = eventInfoChangeManagerService.infoIschange(redisKey, mapKey3, info.getUsedSize());
        if (flag3) {
            ChangeInfo changeInfo = new ChangeInfo();
            changeInfo.setValue(info.getTotalSize());
            changeInfo.setRedisKey(redisKey);
            changeInfo.setMapKey(mapKey3);
            changeInfo.setCollectTime(new Date(info.getCollectTime()));
            info.getMaps().put(mapKey3 + "_" + info.getName(), changeInfo);
        }
        String mapKey4 = StatusInfoChangeTypeEnum.status_tablespace_usedRate.getCode();
        boolean flag4 = eventInfoChangeManagerService.infoIschange(redisKey, mapKey3, info.getUsedRate());
        ChangeInfo changeInfo = new ChangeInfo();
        changeInfo.setValue(info.getUsedRate());
        changeInfo.setIsChange(flag4);
        changeInfo.setRedisKey(redisKey);
        changeInfo.setMapKey(mapKey4);
        changeInfo.setCollectTime(new Date(info.getCollectTime()));
        info.getMaps().put(mapKey4 + "_" + info.getName(), changeInfo);
        return true;
    }

    @Override
    public boolean isNeedNexthandle(Boolean flag) {
        return flag;
    }


}
