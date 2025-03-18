package com.jcca.dataProcessing.DataFilter.process;

import com.jcca.dataProcessing.Entity.ChangeInfo;
import com.jcca.dataProcessing.Entity.CollectProcessEntity;
import com.jcca.dataProcessing.enums.StatusInfoChangeTypeEnum;
import com.jcca.dataProcessing.manager.IEventInfoManagerService;
import com.jcca.dataProcessing.support.IFilterHandler;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;

/**
 * @author Zhaozheng
 * @description TODO 进程Id过滤处理类
 * @className ProcessIdFilterHandler
 * @date 2023/10/27 9:58
 * @since 2.1.0.0
 */
@Component("ProcessIdFilterHandler")
public class ProcessIdFilterHandler extends IFilterHandler<CollectProcessEntity> {

    @Resource
    private IEventInfoManagerService eventInfoChangeManagerService;


    @Override
    public boolean handler(CollectProcessEntity info) {
        if (!info.getStatus()) {
            return true;
        }
        String redisKey = info.getAssetIp() + ":" + info.getAssetId() + ":" + StatusInfoChangeTypeEnum.status_process.getCode() + ":" + info.getName();
        String mapKey = StatusInfoChangeTypeEnum.status_process_id.getCode();
        //判断数据是否有变化
        boolean flag = eventInfoChangeManagerService.infoIschange(redisKey, mapKey, info.getProcessId());
        if (flag) {
            ChangeInfo changeInfo = new ChangeInfo();
            changeInfo.setValue(info.getProcessId());
            changeInfo.setRedisKey(redisKey);
            changeInfo.setIsChange(true);
            changeInfo.setMapKey(mapKey);
            changeInfo.setCollectTime(new Date());
            info.getMaps().put(mapKey, changeInfo);
        }
        return true;
    }

    @Override
    public boolean isNeedNexthandle(Boolean flag) {
        return flag;
    }


}
