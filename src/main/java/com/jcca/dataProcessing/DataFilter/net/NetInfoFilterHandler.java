package com.jcca.dataProcessing.DataFilter.net;

import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.utils.AppLogUtils;
import com.jcca.dataProcessing.Entity.ChangeInfo;
import com.jcca.dataProcessing.Entity.CollectNetworkCardEntity;
import com.jcca.dataProcessing.enums.StatusInfoChangeTypeEnum;
import com.jcca.dataProcessing.manager.IEventInfoManagerService;
import com.jcca.dataProcessing.support.IFilterHandler;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;

/**
 * @author Zhaozheng
 * @description TODO 网卡信息过滤处理类
 * @className NetInfoFitlerHandler
 * @date 2023/10/27 9:46
 * @since 2.1.0.0
 */
@Component("netInfoFilterHandler")
public class NetInfoFilterHandler extends IFilterHandler<CollectNetworkCardEntity> {
    @Resource
    private IEventInfoManagerService eventInfoChangeManagerService;

    @Override
    public boolean handler(CollectNetworkCardEntity info) {
        AppLogUtils.buildLogInfo(LogFunctionEnum.DATA_PROCESS_SINGLE, "网卡信息过滤处理类", info.getAssetIp());
        String redisKey = info.getAssetIp() + ":" + info.getAssetId() + ":" + StatusInfoChangeTypeEnum.status_net.getCode() + ":" + info.getName();
        String mapKey1 = StatusInfoChangeTypeEnum.status_net_status.getCode();
        String mapKey2 = StatusInfoChangeTypeEnum.status_net_portIn.getCode();
        String mapKey3 = StatusInfoChangeTypeEnum.status_net_portOut.getCode();
        String mapKey4 = StatusInfoChangeTypeEnum.status_net_portInSpeed.getCode();
        String mapKey5 = StatusInfoChangeTypeEnum.status_net_portOutSpeed.getCode();
        String mapKey6 = StatusInfoChangeTypeEnum.status_net_macAddress.getCode();
        String mapKey7 = StatusInfoChangeTypeEnum.status_net_ip.getCode();


        boolean flag2 = eventInfoChangeManagerService.infoIschange(redisKey, mapKey2, info.getPortIn());
        boolean flag3 = eventInfoChangeManagerService.infoIschange(redisKey, mapKey3, info.getPortOut());
        boolean flag4 = eventInfoChangeManagerService.infoIschange(redisKey, mapKey4, info.getPortInSpeed());
        boolean flag5 = eventInfoChangeManagerService.infoIschange(redisKey, mapKey5, info.getPortOutSpeed());
        boolean flag6 = eventInfoChangeManagerService.infoIschange(redisKey, mapKey6, info.getMacAddress());
        boolean flag7 = eventInfoChangeManagerService.infoIschange(redisKey, mapKey7, info.getIp());
        //在下一个状态处理类中处理
        if (true) {
            ChangeInfo changeInfo = new ChangeInfo();
            changeInfo.setValue(info.getStatus());
            changeInfo.setRedisKey(redisKey);
            changeInfo.setMapKey(mapKey1);
            changeInfo.setCollectTime(new Date(info.getCollectTime()));
            info.getMaps().put(mapKey1, changeInfo);
        }
        if (flag2) {
            ChangeInfo changeInfo = new ChangeInfo();
            changeInfo.setValue(info.getPortIn());
            changeInfo.setRedisKey(redisKey);
            changeInfo.setMapKey(mapKey2);
            changeInfo.setCollectTime(new Date(info.getCollectTime()));
            info.getMaps().put(mapKey2, changeInfo);
        }
        if (flag3) {
            ChangeInfo changeInfo = new ChangeInfo();
            changeInfo.setValue(info.getPortOut());
            changeInfo.setRedisKey(redisKey);
            changeInfo.setMapKey(mapKey3);
            changeInfo.setCollectTime(new Date(info.getCollectTime()));
            info.getMaps().put(mapKey3, changeInfo);
        }
        if (flag4) {
            ChangeInfo changeInfo = new ChangeInfo();
            changeInfo.setValue(info.getPortInSpeed());
            changeInfo.setRedisKey(redisKey);
            changeInfo.setMapKey(mapKey4);
            changeInfo.setCollectTime(new Date(info.getCollectTime()));
            info.getMaps().put(mapKey4, changeInfo);
        }
        if (flag5) {
            ChangeInfo changeInfo = new ChangeInfo();
            changeInfo.setValue(info.getPortOutSpeed());
            changeInfo.setRedisKey(redisKey);
            changeInfo.setMapKey(mapKey5);
            changeInfo.setCollectTime(new Date(info.getCollectTime()));
            info.getMaps().put(mapKey5, changeInfo);
        }
        if (flag6) {
            ChangeInfo changeInfo = new ChangeInfo();
            changeInfo.setValue(info.getMacAddress());
            changeInfo.setRedisKey(redisKey);
            changeInfo.setMapKey(mapKey6);
            changeInfo.setCollectTime(new Date(info.getCollectTime()));
            info.getMaps().put(mapKey6, changeInfo);
        }
        if (flag7) {
            ChangeInfo changeInfo = new ChangeInfo();
            changeInfo.setValue(info.getIp());
            changeInfo.setRedisKey(redisKey);
            changeInfo.setMapKey(mapKey7);
            changeInfo.setCollectTime(new Date(info.getCollectTime()));
            info.getMaps().put(mapKey7, changeInfo);
        }

        return true;
    }

    @Override
    public boolean isNeedNexthandle(Boolean flag) {
        return flag;
    }


}
