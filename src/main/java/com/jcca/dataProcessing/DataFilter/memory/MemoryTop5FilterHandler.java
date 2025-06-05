package com.jcca.dataProcessing.DataFilter.memory;

import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.utils.AppLogUtils;
import com.jcca.dataProcessing.Entity.CollectMemoryEntity;
import com.jcca.dataProcessing.Entity.CollectProcessEntity;
import com.jcca.dataProcessing.enums.StatusInfoChangeTypeEnum;
import com.jcca.dataProcessing.manager.IEventInfoManagerService;
import com.jcca.dataProcessing.support.IFilterHandler;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author Zhaozheng
 * @description TODO 内存top5信息过滤处理类
 * @className MemoryTop5FilterHandler
 * @date 2023/10/27 9:44
 * @since 2.1.0.0
 */
@Component("memoryTop5FilterHandler")
public class MemoryTop5FilterHandler  extends IFilterHandler<CollectMemoryEntity> {
    @Resource
    private IEventInfoManagerService eventInfoChangeManagerService;
    @Override
    public boolean handler(CollectMemoryEntity info) {
        AppLogUtils.buildLogInfo(LogFunctionEnum.DATA_PROCESS_SINGLE, "内存TOP5阈值信息过滤处理类", info.getAssetIp());
        String redisKey= info.getAssetIp()+":"+info.getAssetId()+":"+ StatusInfoChangeTypeEnum.status_MEMTop5.getCode();
        List<CollectProcessEntity> list=info.getProcessTop5List();
        boolean flag=false;
        if(list!=null&&list.size()>0){
            for(CollectProcessEntity collectProcessEntity:list){
                if( eventInfoChangeManagerService.infoIschange(info.getInspectRecordId(),redisKey,collectProcessEntity.getName()+"_"+collectProcessEntity.getProcessId(),collectProcessEntity.getMemoryRate())){
                    flag=true;
                    break;
                }
            }
        }
        if(flag){
            eventInfoChangeManagerService.delRedisKey(redisKey);
            for(CollectProcessEntity collectProcessEntity:list){
                eventInfoChangeManagerService.setStateValue(redisKey,collectProcessEntity.getName()+"_"+collectProcessEntity.getProcessId(),collectProcessEntity.getMemoryRate());
            }
        }
        return true;
    }

    @Override
    public boolean isNeedNexthandle(Boolean flag) {
        return flag;
    }

}
