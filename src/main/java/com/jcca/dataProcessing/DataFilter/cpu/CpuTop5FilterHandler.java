package com.jcca.dataProcessing.DataFilter.cpu;

import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.utils.AppLogUtils;
import com.jcca.dataProcessing.Entity.CollectCpuEntity;
import com.jcca.dataProcessing.Entity.CollectProcessEntity;
import com.jcca.dataProcessing.enums.StatusInfoChangeTypeEnum;
import com.jcca.dataProcessing.manager.IEventInfoManagerService;
import com.jcca.dataProcessing.support.IFilterHandler;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author Zhaozheng
 * @description TODO CPU TOP5 过滤处理类
 * @className CpuTop5FilterHandler
 * @date 2023/10/27 9:26
 * @since 2.1.0.0
 */
@Component("CpuTop5FilterHandler")
public class CpuTop5FilterHandler  extends IFilterHandler<CollectCpuEntity> {
    @Resource
    private IEventInfoManagerService eventInfoChangeManagerService;
    @Override
    public boolean handler(CollectCpuEntity info) {
        AppLogUtils.buildLogInfo(LogFunctionEnum.DATA_PROCESS_SINGLE, "CPU TOP5阈值处理类", info.getAssetIp());
        String redisKey= info.getAssetIp()+":"+info.getAssetId()+":"+ StatusInfoChangeTypeEnum.status_CPUTop5.getCode();
        List<CollectProcessEntity> list=info.getProcessTop5List();
        boolean flag=false;
        if(list!=null&&list.size()>0){
            for(CollectProcessEntity collectProcessEntity:list){
               if( eventInfoChangeManagerService.infoIschange(info.getInspectRecordId(),redisKey,collectProcessEntity.getName()+"_"+collectProcessEntity.getProcessId(),collectProcessEntity.getCpuRate())){
                    flag=true;
                    break;
                }
            }
        }
        if(flag){
            eventInfoChangeManagerService.delRedisKey(redisKey);
            for(CollectProcessEntity collectProcessEntity:list){
                 eventInfoChangeManagerService.setStateValue(redisKey,collectProcessEntity.getName()+"_"+collectProcessEntity.getProcessId(),collectProcessEntity.getCpuRate());
            }
        }
        return true;
    }

    @Override
    public boolean isNeedNexthandle(Boolean flag) {

        return flag;
    }

}
