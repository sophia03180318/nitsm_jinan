package com.jcca.dataProcessing.DataFilter.casco;

import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.utils.AppLogUtils;
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
 * @description TODO casco容量信息过滤处理类
 * @className CascoThresholdFilterHandler
 * @date 2023/10/27 9:24
 * @since 2.1.0.0
 */
@Component("cascoThresholdFilterHandler")
public class CascoThresholdFilterHandler extends IFilterHandler<ItsmQueueEntity> {

    @Resource
    private IEventInfoManagerService eventInfoChangeManagerService;

    @Override
    public boolean handler(ItsmQueueEntity info) {
        AppLogUtils.buildLogInfo(LogFunctionEnum.DATA_PROCESS_SINGLE, "casco容量信息过滤处理类", info.getAssetIp());
        String redisKey = info.getAssetIp() + ":" + info.getAssetId() + ":" + StatusInfoChangeTypeEnum.status_softThresholdState.getCode();
        String mapKey = info.getAssetId() + "_" + info.getAttrGroupId() + "_" + info.getAttrIndex();
        ChangeInfo changeInfo = new ChangeInfo();
        changeInfo.setValue(info.getCollectValue());
        changeInfo.setRedisKey(redisKey);
        changeInfo.setMapKey(mapKey);
        changeInfo.setCollectTime(new Date());
        info.getMaps().put(mapKey, changeInfo);
        String eventRedisKey = StatusInfoChangeTypeEnum.event_CTC_threshold.getCode();
        String eventMapKey = info.getAssetIp() + "_" + info.getAssetId() + "_" + info.getEntityId() + "_" + info.getAbFlag() + "_" + info.getAttrGroupId() + "_" + info.getAttrIndex();
        Integer status = Integer.parseInt(info.getCollectValue()) > Integer.parseInt(info.getBaseValue()) ? EventLevelEnum.ABNORMAL.getCode() : EventLevelEnum.NORMAL.getCode();
        String str = status == EventLevelEnum.ABNORMAL.getCode() ? "超过" : "";
        //添加状态监控（设备监控的事件信息是否正常）
        this.addEventStatus(StatusInfoChangeTypeEnum.event_CTC_threshold.getCode(), StatusInfoChangeTypeEnum.THRESHOLD_STATUS.getCode(), info.getEntityId() + "_" + info.getAbFlag(), status, info, changeInfo);
        AlarmTempReq alarmTempReq = new AlarmTempReq();
        alarmTempReq.setOrgMsg(String.format(StatusInfoChangeTypeEnum.event_CTC_threshold.getDescr(), info.getCollectValue(), str, info.getBaseValue()));
        alarmTempReq.setCollectValue(info.getCollectValue());
        alarmTempReq.setThresholdValue(info.getBaseValue());
        alarmTempReq.setFlag(mapKey);
        IEvent event = eventInfoChangeManagerService.creatChangeEvent(info.getAssetId(), changeInfo, eventRedisKey, eventMapKey, status, alarmTempReq);
        if (event != null) {
            //被事件信息截取
            event.setDescStr(String.format(StatusInfoChangeTypeEnum.event_CTC_threshold.getDescr(), info.getCollectValue(), str, info.getBaseValue()));
            changeInfo.setIsEvent(true);
            this.dispatureEvent(event);
        }
        return true;
    }

    @Override
    public boolean isNeedNexthandle(Boolean flag) {
        return flag;
    }


}
