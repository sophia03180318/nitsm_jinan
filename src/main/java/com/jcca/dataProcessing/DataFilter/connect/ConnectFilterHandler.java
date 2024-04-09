package com.jcca.dataProcessing.DataFilter.connect;

import com.jcca.dataProcessing.Entity.ChangeInfo;
import com.jcca.dataProcessing.Entity.CollectConnectEntity;
import com.jcca.dataProcessing.enums.StatusInfoChangeTypeEnum;
import com.jcca.dataProcessing.manager.IEventInfoManagerService;
import com.jcca.dataProcessing.support.IFilterHandler;
import com.jcca.web.asset.service.AssetService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;

/**
 * @author Zhaozheng
 * @description TODO 连接数过滤处理类
 * @className ConnectFitlerHandler
 * @date 2023/10/27 9:26
 * @since 2.1.0.0
 */
@Component("connectFilterHandler")
public class ConnectFilterHandler extends IFilterHandler<CollectConnectEntity> {

    @Resource
    private IEventInfoManagerService eventInfoChangeManagerService;
    @Resource
    private AssetService assetServ;

    @Override
    public boolean handler(CollectConnectEntity info) {
        String redisKey= info.getAssetIp()+":"+info.getAssetId()+":"+StatusInfoChangeTypeEnum.status.getCode();
        String mapKey=StatusInfoChangeTypeEnum.status_establishedNum.getCode();

        boolean flag= eventInfoChangeManagerService.infoIschange(redisKey, mapKey,info.getEstablishedNum());
        if(flag) {
            ChangeInfo changeInfo = new ChangeInfo();
            changeInfo.setValue(info.getEstablishedNum());
            changeInfo.setRedisKey(redisKey);
            changeInfo.setMapKey(mapKey);
            changeInfo.setCollectTime(new Date(info.getCollectTime()));
            info.getMaps().put(mapKey, changeInfo);
            return true;
        }
        return true;
    }

    @Override
    public boolean isNeedNexthandle(Boolean flag) {
        return flag;
    }

}
