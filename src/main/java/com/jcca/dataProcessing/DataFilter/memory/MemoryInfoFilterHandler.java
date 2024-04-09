package com.jcca.dataProcessing.DataFilter.memory;

import com.jcca.dataProcessing.Entity.ChangeInfo;
import com.jcca.dataProcessing.Entity.CollectMemoryEntity;
import com.jcca.dataProcessing.enums.StatusInfoChangeTypeEnum;
import com.jcca.dataProcessing.manager.IEventInfoManagerService;
import com.jcca.dataProcessing.support.IFilterHandler;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;

/**
 * @author Zhaozheng
 * @description TODO 内存普通阈值信息过滤处理类
 * @className MemoryFilterHandler
 * @date 2023/10/27 9:44
 * @since 2.1.0.0
 */
@Component("memoryInfoFilterHandler")
public class MemoryInfoFilterHandler extends IFilterHandler<CollectMemoryEntity> {

    @Resource
    private IEventInfoManagerService eventInfoChangeManagerService;

    @Override
    public boolean handler(CollectMemoryEntity info) {
        String redisKey = info.getAssetIp() + ":" + info.getAssetId() + ":" + StatusInfoChangeTypeEnum.status.getCode();
        String mapKey = StatusInfoChangeTypeEnum.status_memory_total.getCode();
        String usedKey = StatusInfoChangeTypeEnum.status_memory_used.getCode();
        String switchTotalKey = StatusInfoChangeTypeEnum.status_switch_memory_total.getCode();
        String switchUsedKey = StatusInfoChangeTypeEnum.status_switch_memory_used.getCode();

        //判断数据是否有变化
        boolean flag = eventInfoChangeManagerService.infoIschange(redisKey, mapKey, info.getMemTotal());
        if(flag){
            ChangeInfo changeInfo = new ChangeInfo();
            changeInfo.setValue(info.getMemTotal());
            changeInfo.setIsChange(true);
            changeInfo.setCollectTime(new Date(info.getCollectTime()));
            changeInfo.setRedisKey(redisKey);
            changeInfo.setMapKey(mapKey);
            info.getMaps().put(mapKey, changeInfo);
        }

        boolean usedFlag = eventInfoChangeManagerService.infoIschange(redisKey, usedKey, info.getMemUsed());

        if(usedFlag){
            ChangeInfo changeInfo = new ChangeInfo();
            changeInfo.setValue(info.getMemUsed());
            changeInfo.setIsChange(true);
            changeInfo.setCollectTime(new Date(info.getCollectTime()));
            changeInfo.setRedisKey(redisKey);
            changeInfo.setMapKey(usedKey);
            info.getMaps().put(usedKey, changeInfo);
        }


        boolean switchTotalFlag = eventInfoChangeManagerService.infoIschange(redisKey, switchTotalKey, info.getSwapTotal());

        if(switchTotalFlag){
            ChangeInfo changeInfo = new ChangeInfo();
            changeInfo.setValue(info.getSwapTotal());
            changeInfo.setIsChange(true);
            changeInfo.setCollectTime(new Date(info.getCollectTime()));
            changeInfo.setRedisKey(redisKey);
            changeInfo.setMapKey(switchTotalKey);
            info.getMaps().put(switchTotalKey, changeInfo);
        }

        boolean switchUsedFlag = eventInfoChangeManagerService.infoIschange(redisKey, switchUsedKey, info.getSwapUsed());

        if(switchUsedFlag) {
            ChangeInfo changeInfo = new ChangeInfo();
            changeInfo.setValue(info.getSwapUsed());
            changeInfo.setIsChange(true);
            changeInfo.setCollectTime(new Date(info.getCollectTime()));
            changeInfo.setRedisKey(redisKey);
            changeInfo.setMapKey(switchUsedKey);
            info.getMaps().put(switchUsedKey, changeInfo);
        }

        return true;


    }

    @Override
    public boolean isNeedNexthandle(Boolean flag) {
        return flag;
    }


}
