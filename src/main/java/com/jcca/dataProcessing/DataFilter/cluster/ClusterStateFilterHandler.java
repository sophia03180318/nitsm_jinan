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
 * @description TODO 自律机状态过滤处理类
 * @className ClusterABStatusFilterHandler
 * @date 2023/10/20 17:24
 * @since 2.1.0.0
 */
@Component("clusterStateFilterHandler")
public class ClusterStateFilterHandler extends IFilterHandler<CollectClusterEntity> {

    @Resource
    private IEventInfoManagerService eventInfoChangeManagerService;

    @Override
    public boolean handler(CollectClusterEntity info) {


        String redisKey = info.getAssetIp() + ":" + info.getAssetId() + ":" + StatusInfoChangeTypeEnum.status.getCode() + ":" + info.getServerIp();
        String mapKey1 = StatusInfoChangeTypeEnum.status_clusterState.getCode();

        boolean flag1= eventInfoChangeManagerService.infoIschange(info.getInspectRecordId(),redisKey, mapKey1,info.getStatus());
        if(flag1) {
            ChangeInfo changeInfo = new ChangeInfo();
            changeInfo.setValue(info.getStatus());
            Date date = new Date();
            date.setTime(Long.valueOf(info.getCollectTimeStr()));
            changeInfo.setCollectTime(date);
            changeInfo.setRedisKey(redisKey);
            changeInfo.setMapKey(mapKey1);
            info.getMaps().put(mapKey1, changeInfo);

            Integer status = changeInfo.getValue().equals("OK") ? EventLevelEnum.NORMAL.getCode() : EventLevelEnum.ABNORMAL.getCode();
            String format = String.format(StatusInfoChangeTypeEnum.event_clusterMasterState.getDescr(), info.getServerName());

            String eventRedisKey = StatusInfoChangeTypeEnum.event_clusterState.getCode();
            String eventMapKey = info.getAssetIp() + "_" + info.getAssetId() + "_" + info.getServerIp();
            AlarmTempReq alarmTempReq = new AlarmTempReq();
            alarmTempReq.setOrgMsg(format);
            alarmTempReq.setCollectValue(changeInfo.getValue().toString());
            alarmTempReq.setFlag(info.getServerName());
            //添加状态监控（设备监控的事件信息是否正常）
            this.addEventStatus(StatusInfoChangeTypeEnum.event_clusterState.getCode(),StatusInfoChangeTypeEnum.ZLJ_STATUS.getCode(),info.getServerName(), status, info, changeInfo);
            IEvent event = eventInfoChangeManagerService.creatChangeEvent(info.getAssetId(), changeInfo, eventRedisKey, eventMapKey, status,alarmTempReq,info.getInspectRecordId());
            if (event != null) {
                //被事件信息截取
                changeInfo.setIsEvent(true);
                event.setDescStr(format);
                this.dispatureEvent(event);
            }

            return true;
        }
        return true;

    }

    @Override
    public boolean isNeedNextHandle(Boolean flag) {
        return flag;
    }

}
