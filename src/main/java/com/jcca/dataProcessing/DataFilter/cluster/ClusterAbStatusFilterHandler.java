package com.jcca.dataProcessing.DataFilter.cluster;

import com.jcca.dataProcessing.Entity.ChangeInfo;
import com.jcca.dataProcessing.Entity.CollectClusterEntity;
import com.jcca.dataProcessing.enums.StatusInfoChangeTypeEnum;
import com.jcca.dataProcessing.manager.IEventInfoManagerService;
import com.jcca.dataProcessing.manager.bean.AlarmTempReq;
import com.jcca.dataProcessing.support.IEvent;
import com.jcca.dataProcessing.support.IFilterHandler;
import com.jcca.web.event.enums.EventLevelEnum;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;

/**
 * @author Zhaozheng
 * @description TODO 集群AB机状态过滤处理类
 * @className ClusterABStatusFilterHandler
 * @date 2023/10/20 17:24
 * @since 2.1.0.0
 */
@Component("clusterAbStatusFilterHandler")
public class ClusterAbStatusFilterHandler extends IFilterHandler<CollectClusterEntity> {

    @Resource
    private IEventInfoManagerService eventInfoChangeManagerService;

    @Override
    public boolean handler(CollectClusterEntity info) {

        String redisKey = info.getAssetIp() + ":" + info.getAssetId() + ":" + StatusInfoChangeTypeEnum.status.getCode() + ":" + info.getServerIp();
        String mapKey1 = StatusInfoChangeTypeEnum.status_clusterABState.getCode();


        boolean flag1 = eventInfoChangeManagerService.infoIschange(redisKey, mapKey1, info.getServerName());
        if(flag1) {
            ChangeInfo changeInfo = new ChangeInfo();
            changeInfo.setValue(info.getServerName());
            Date date = new Date();
            date.setTime(Long.valueOf(info.getCollectTimeStr()));
            changeInfo.setCollectTime(date);
            changeInfo.setRedisKey(redisKey);
            changeInfo.setMapKey(mapKey1);
            info.getMaps().put(mapKey1, changeInfo);
            String eventRedisKey = StatusInfoChangeTypeEnum.event_clusterABState.getCode();
            String eventMapKey = info.getAssetIp() + "_" + info.getAssetId() + "_" + info.getServerIp();
            String keyWord = String.format(StatusInfoChangeTypeEnum.event_clusterABState.getDescr(), info.getServerName());
            AlarmTempReq alarmTempReq = new AlarmTempReq();
            alarmTempReq.setOrgMsg(keyWord);
            alarmTempReq.setCollectValue(info.getServerRole());
            alarmTempReq.setFlag(info.getServerName());
            IEvent event = eventInfoChangeManagerService.creatChangeEvent(info.getAssetId(), changeInfo, eventRedisKey, eventMapKey, EventLevelEnum.ABNORMAL.getCode(),alarmTempReq);
            if (event != null) {
                //被事件信息截取
                changeInfo.setIsEvent(true);
                event.setDescStr(keyWord);
                this.dispatureEvent(event);
            }

            return true;
        }
         return true;
    }

    @Override
    public boolean isNeedNexthandle(Boolean flag) {
        return flag;
    }



}
