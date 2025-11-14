package com.jcca.dataProcessing.DataFilter.DB;

import com.jcca.dataProcessing.Entity.ChangeInfo;
import com.jcca.dataProcessing.Entity.CollectTablespaceEntity;
import com.jcca.dataProcessing.Entity.ThresholdBaseEntity;
import com.jcca.dataProcessing.enums.StatusInfoChangeTypeEnum;
import com.jcca.dataProcessing.manager.IEventInfoManagerService;
import com.jcca.dataProcessing.manager.bean.AlarmTempReq;
import com.jcca.dataProcessing.manager.threshold.ThresholdManager;
import com.jcca.dataProcessing.support.IEvent;
import com.jcca.dataProcessing.support.IFilterHandler;
import com.jcca.web.event.enums.EventLevelEnum;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author Zhaozheng
 * @description TODO 数据库表空间阈值过滤处理类
 * @className DBTableSpaceFilterHandler
 * @date 2023/10/27 9:30
 * @since 2.1.0.0
 */
@Component("dBTableSpaceFilterHandler")
public class DbTableSpaceFilterHandler extends IFilterHandler<CollectTablespaceEntity> {

    @Resource
    private IEventInfoManagerService eventInfoChangeManagerService;
    @Resource
    private ThresholdManager thresholdManager;

    @Override
    public boolean handler(CollectTablespaceEntity info) {
        ChangeInfo changeInfo = info.getMaps().get(StatusInfoChangeTypeEnum.status_tablespace_usedRate.getCode() + "_" + info.getName());
        String eventRedisKey = StatusInfoChangeTypeEnum.event_db_tableSpace_normal.getCode();
        String eventMapKey = info.getAssetIp() + "_" + info.getAssetId() + "_" + info.getName();

        String redisThresholdKey =thresholdManager.getThresholdRedisKey(info.getAssetId(),info.getAssetIp());
        String thresholdMapKey = thresholdManager.getThresholdMapKey(StatusInfoChangeTypeEnum.event_db_tableSpace_normal.getCode(),StatusInfoChangeTypeEnum.NORMAL.getCode(),info.getName());

        ThresholdBaseEntity threshold = thresholdManager.getThresholdValue(StatusInfoChangeTypeEnum.event_db_tableSpace.getCode(), info.getAssetId(), "");
        if (threshold.baseValueIsNull()) {
            IEvent event = eventInfoChangeManagerService.creatRecoveryThresholdEvent(info.getAssetId(), changeInfo, eventRedisKey, eventMapKey, redisThresholdKey, thresholdMapKey,info.getInspectRecordId());
            if (event != null) {
                this.dispatureEvent(event);
            }
            return true;
        }

        boolean thresholdFlag = eventInfoChangeManagerService.infoIschange(info.getInspectRecordId(),redisThresholdKey, thresholdMapKey, threshold.getBaseValue());
        if (thresholdFlag) {
            this.addThresholdStatus(redisThresholdKey,thresholdMapKey, threshold.getBaseValue(), info);
        }

        //如果性能数据有变动或者阈值有变动,则需要重新推送事件信息
        if (changeInfo.getIsChange() || thresholdFlag) {
            boolean compare = threshold.getBaseValue() < info.getUsedRate();
            Integer status = compare ? EventLevelEnum.ABNORMAL.getCode() : EventLevelEnum.NORMAL.getCode();

            AlarmTempReq alarmTempReq = new AlarmTempReq();
            String keyWord = status.equals(EventLevelEnum.NORMAL.getCode()) ? "" : "超过";
            alarmTempReq.setOrgMsg(String.format(StatusInfoChangeTypeEnum.event_db_tableSpace_normal.getDescr(), info.getName(), changeInfo.getValue(), keyWord, threshold.getBaseValue()));
            alarmTempReq.setCollectValue(changeInfo.getValue()+"%");
            alarmTempReq.setCollectValue(threshold.getBaseValue()+"%");
            alarmTempReq.setFlag(info.getName());
            //添加状态监控（设备监控的事件信息是否正常）
            this.addEventStatus(StatusInfoChangeTypeEnum.event_db_tableSpace_normal.getCode(),StatusInfoChangeTypeEnum.NORMAL_VAL.getCode(),info.getName(), status, info, changeInfo);
            IEvent event = eventInfoChangeManagerService.creatChangeEvent(info.getAssetId(), changeInfo, eventRedisKey, eventMapKey, status,alarmTempReq,info.getInspectRecordId(),info.getVersion());
            if (event != null) {
                //被事件信息截取
                changeInfo.setIsEvent(true);
                event.setDescStr(String.format(StatusInfoChangeTypeEnum.event_db_tableSpace_normal.getDescr(), info.getName(), changeInfo.getValue(), keyWord, threshold.getBaseValue()));

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
