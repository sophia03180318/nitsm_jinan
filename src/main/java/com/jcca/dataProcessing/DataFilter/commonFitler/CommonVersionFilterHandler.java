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
 * @description TODO casco版本变化过滤处理类
 * @className CascoVersionFilterHandler
 * @date 2023/10/27 9:25
 * @since 2.1.0.0
 */
@Component("commonVersionFilterHandler")
public class CommonVersionFilterHandler extends IFilterHandler<ItsmQueueEntity> {

    @Resource
    private IEventInfoManagerService eventInfoChangeManagerService;

    @Override
    public boolean handler(ItsmQueueEntity info) {
        String redisKey = info.getAssetIp() + ":" + info.getAssetId() + ":" + StatusInfoChangeTypeEnum.status_softVersion.getCode();
        String mapKey = info.getAssetId() + "_" + info.getEntityId() + "_" + info.getAbFlag();

        Boolean flag = eventInfoChangeManagerService.infoIschangeFirst(info.getInspectRecordId(),redisKey, mapKey, info.getNowVersion());
        if (flag == null || flag == true) {
            ChangeInfo changeInfo = new ChangeInfo();
            changeInfo.setValue(info.getNowVersion());
            changeInfo.setRedisKey(redisKey);
            changeInfo.setMapKey(mapKey);
            changeInfo.setCollectTime(new Date());
            info.getMaps().put(mapKey, changeInfo);
            if (flag != null && flag == true) {
                String eventRedisKey = StatusInfoChangeTypeEnum.event_CTC_version.getCode();
                String eventMapKey = info.getAssetIp() + "_" + info.getAssetId() + "_" + info.getEntityId() + "_" + info.getAbFlag();

                AlarmTempReq alarmTempReq = new AlarmTempReq();
                alarmTempReq.setOrgMsg(String.format(StatusInfoChangeTypeEnum.event_CTC_version.getDescr(), info.getCascoSoftName(), info.getOldVersion(), info.getNowVersion()));
                alarmTempReq.setCollectValue(info.getNowVersion());
                alarmTempReq.setThresholdValue(info.getOldVersion());

                this.addEventStatus(StatusInfoChangeTypeEnum.event_CTC_version.getCode(), StatusInfoChangeTypeEnum.STATUS.getCode(), "", EventLevelEnum.ABNORMAL.getCode(), info, changeInfo);
                IEvent event = eventInfoChangeManagerService.creatChangeEvent(info.getAssetId(), changeInfo, eventRedisKey, eventMapKey, EventLevelEnum.ABNORMAL.getCode(), alarmTempReq,info.getInspectRecordId());
                //被事件信息截取
                changeInfo.setIsEvent(true);
                //北洋软件的连接有collectValue描述信息
                if (info.getCollectValue() != null && !"".equals(info.getCollectValue())) {
                    event.setDescLog(info.getCollectValue());
                } else {
                    event.setDescStr(String.format(StatusInfoChangeTypeEnum.event_CTC_version.getDescr(), info.getCascoSoftName(), info.getOldVersion(), info.getNowVersion()));
                }
                this.dispatureEvent(event);
            }

        }

        return true;
    }

    @Override
    public boolean isNeedNextHandle(Boolean flag) {
        return flag;
    }


}
