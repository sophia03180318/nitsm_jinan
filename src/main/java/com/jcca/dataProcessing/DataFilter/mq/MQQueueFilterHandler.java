package com.jcca.dataProcessing.DataFilter.mq;

import com.jcca.dataProcessing.Entity.ChangeInfo;
import com.jcca.dataProcessing.Entity.MQMonitorEntity;
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
 * @description TODO
 * @className MQQueueFilterHandler
 * @date 2024/1/6 10:36
 * @since 2.1.0.0
 */
@Component("mqQueueFilterHandler")
public class MQQueueFilterHandler extends IFilterHandler<MQMonitorEntity> {
    @Resource
    private IEventInfoManagerService eventInfoChangeManagerService;

    @Override
    public boolean handler(MQMonitorEntity info) {
        //name来判断是否为mq队列的信息
        if (info.getName() == null) {
            return true;
        }

        String redisKey = info.getAssetIp() + ":" + info.getAssetId() + ":" + StatusInfoChangeTypeEnum.status_mq_queue.getCode();
        String mapKey = info.getName();

        //判断数据是否有变化
        ChangeInfo changeInfo = null;
        boolean flag = eventInfoChangeManagerService.infoIschange(info.getInspectRecordId(),redisKey, mapKey, info.getStatus());
        if (flag) {
            changeInfo = new ChangeInfo();
            changeInfo.setValue(info.getStatus());
            changeInfo.setIsChange(true);
            changeInfo.setCollectTime(new Date(info.getCollectTime()));
            changeInfo.setRedisKey(redisKey);
            changeInfo.setMapKey(mapKey);
            info.getMaps().put(mapKey, changeInfo);
        }


        String eventRedisKey = StatusInfoChangeTypeEnum.event_mq_queue.getCode();
        String eventMapKey = info.getAssetIp() + "_" + info.getAssetId() + "_" + info.getName();

        if (flag) {
            //状态信息有变化
            Integer status = info.getStatus() == EventLevelEnum.ABNORMAL.getCode() ? EventLevelEnum.ABNORMAL.getCode() : EventLevelEnum.NORMAL.getCode();

            AlarmTempReq tempReq = new AlarmTempReq();
            tempReq.setOrgMsg(info.getMessage());
            tempReq.setCollectValue(info.getStatus().toString());
            tempReq.setFlag(info.getName());


            IEvent event = eventInfoChangeManagerService.creatChangeEvent(info.getAssetId(), changeInfo, eventRedisKey, eventMapKey, status,tempReq,info.getInspectRecordId());
            if (event != null) {
                //被事件信息截取
                changeInfo.setIsEvent(true);
                event.setDescStr(info.getMessage());
                this.dispatureEvent(event);
            }
        }

        return true;
    }

    @Override
    public boolean isNeedNexthandle(Boolean flag) {
        return flag;
    }
}
