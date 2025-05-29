package com.jcca.dataProcessing.DataFilter.commonFitler;

import com.jcca.dataProcessing.Entity.ChangeInfo;
import com.jcca.dataProcessing.Entity.ItsmQueueEntity;
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
 * @description TODO 北洋软件状态过滤处理类
 * @className BeiyangWorkstateFilterHandler
 * @date 2023/10/27 9:23
 * @since 2.1.0.0
 */
@Component("commonWorkStateFilterHandler")
public class CommonWorkStateFilterHandler extends IFilterHandler<ItsmQueueEntity> {
    @Resource
    private IEventInfoManagerService eventInfoChangeManagerService;

    @Override
    public boolean handler(ItsmQueueEntity info) {

        String redisKey = info.getAssetIp() + ":" + info.getAssetId() + ":" + StatusInfoChangeTypeEnum.status_soft.getCode();
        String mapKey = info.getIdStr();

        boolean flag = eventInfoChangeManagerService.infoIschange(info.getInspectRecordId(),redisKey, mapKey, info.getAlarmState());
        if (flag) {
            ChangeInfo changeInfo = new ChangeInfo();
            changeInfo.setValue(info.getAlarmState());
            changeInfo.setRedisKey(redisKey);
            changeInfo.setMapKey(mapKey);
            changeInfo.setCollectTime(new Date());
            info.getMaps().put(mapKey, changeInfo);
            String eventRedisKey = StatusInfoChangeTypeEnum.event_CTC_runstate.getCode();
            String eventMapKey = info.getAssetIp() + "_" + info.getAssetId() + "_" + info.getIdStr();
            Integer status = info.getAlarmState() == 1 ? EventLevelEnum.NORMAL.getCode() : EventLevelEnum.ABNORMAL.getCode();

            AlarmTempReq alarmTempReq = new AlarmTempReq();
            String str = status == EventLevelEnum.ABNORMAL.getCode() ? "异常。" : "恢复。";
            alarmTempReq.setOrgMsg(String.format(StatusInfoChangeTypeEnum.event_CTC_runstate.getDescr(), info.getAssetIp(), str));
            alarmTempReq.setCollectValue(info.getAlarmState()+"");
            alarmTempReq.setFlag(mapKey);
            IEvent event = eventInfoChangeManagerService.creatChangeEvent(info.getAssetId(), changeInfo, eventRedisKey, eventMapKey, status,alarmTempReq);
            //添加状态监控（设备监控的事件信息是否正常）
            this.addEventStatus(StatusInfoChangeTypeEnum.event_CTC_runstate.getCode(),StatusInfoChangeTypeEnum.RUN_STATUS.getCode(),info.getIdStr(), status, info, changeInfo);
            if (event != null) {
                //被事件信息截取
                changeInfo.setIsEvent(true);
                if (info.getCollectValue() != null && !"".equals(info.getCollectValue())) {
                    event.setDescLog(info.getCollectValue());
                } else {

                    event.setDescStr(String.format(StatusInfoChangeTypeEnum.event_CTC_runstate.getDescr(), info.getAssetIp(), str));
                }
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
