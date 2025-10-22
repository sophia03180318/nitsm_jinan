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
 * @description: 阶段阈值处理类
 * @author: Lvyp
 * @create: 2023/11/02 20:13
 */
@Component("dBTableSpaceStageOneFilterHandler")
public class DBTableSpaceStageOneFilterHandler extends IFilterHandler<CollectTablespaceEntity> {

    @Resource
    private IEventInfoManagerService eventInfoChangeManagerService;
    @Resource
    private ThresholdManager thresholdManager;

    @Override
    public boolean handler(CollectTablespaceEntity info) {
        ChangeInfo changeInfo = info.getMaps().get(StatusInfoChangeTypeEnum.status_tablespace_usedRate.getCode() + "_" + info.getName());
        String eventRedisKey = StatusInfoChangeTypeEnum.event_tableSpace_sectionOne.getCode();
        String eventMapKey = info.getAssetIp() + "_" + info.getAssetId() + "_" + info.getName();

        String redisThresholdKey =thresholdManager.getThresholdRedisKey(info.getAssetId(),info.getAssetIp());
        String thresholdMapKey = thresholdManager.getThresholdMapKey(StatusInfoChangeTypeEnum.event_tableSpace_sectionOne.getCode(),StatusInfoChangeTypeEnum.SECTION_ONE.getCode(),info.getName());

        ThresholdBaseEntity threshold = thresholdManager.getThresholdValue(StatusInfoChangeTypeEnum.event_db_tableSpace.getCode(), info.getAssetId(),"");
        if (threshold.oneLevelIsNull()) {
            IEvent event = eventInfoChangeManagerService.creatRecoveryThresholdEvent(info.getAssetId(), changeInfo, eventRedisKey, eventMapKey, redisThresholdKey, thresholdMapKey,info.getInspectRecordId());
            if (event != null) {
                this.dispatureEvent(event);
            }
            return true;
        }

        boolean thresholdFlag = eventInfoChangeManagerService.infoIschange(info.getInspectRecordId(),redisThresholdKey, thresholdMapKey, threshold.getOneLevelValue());
        if (thresholdFlag) {
            this.addThresholdStatus(redisThresholdKey,thresholdMapKey, threshold.getBaseValue(), info);
        }

        boolean compare = threshold.getOneLevelValue() < (Double) changeInfo.getValue();
        //如果性能数据有变动或者阈值有变动,则需要重新推送事件信息
        if (changeInfo.getIsChange() || thresholdFlag) {

            Integer status = compare ? EventLevelEnum.ABNORMAL.getCode() : EventLevelEnum.NORMAL.getCode();
            String keyWord = status.equals(EventLevelEnum.NORMAL.getCode()) ? "" : "超过";
            AlarmTempReq alarmTempReq = new AlarmTempReq();
            alarmTempReq.setOrgMsg(String.format(StatusInfoChangeTypeEnum.event_tableSpace_sectionOne.getDescr(), info.getName(), changeInfo.getValue(), keyWord, threshold.getOneLevelValue()));
            alarmTempReq.setCollectValue(changeInfo.getValue()+"%");
            alarmTempReq.setThresholdValue(threshold.getOneLevelValue()+"%");
            alarmTempReq.setFlag(info.getName());
            //添加状态监控（设备监控的事件信息是否正常）
            this.addEventStatus(StatusInfoChangeTypeEnum.event_tableSpace_sectionOne.getCode(),StatusInfoChangeTypeEnum.SECTION_ONE_VAL.getCode(),info.getName(), status, info, changeInfo);
            IEvent event = eventInfoChangeManagerService.creatChangeEvent(info.getAssetId(), changeInfo, eventRedisKey, eventMapKey, status,alarmTempReq,info.getInspectRecordId());
            if (event != null) {
                //被事件信息截取
                changeInfo.setIsEvent(true);

                event.setDescStr(String.format(StatusInfoChangeTypeEnum.event_tableSpace_sectionOne.getDescr(), info.getName(), changeInfo.getValue(), keyWord, threshold.getOneLevelValue()));
                this.dispatureEvent(event);
            }
        }
        //一阶、二阶、三阶阈值告警信息，命中哪一个就是哪一个不会再命中其他的处理类
        info.setHaveSpecStageEvent(compare);

        return true;
    }

    @Override
    public boolean isNeedNextHandle(Boolean flag) {
        return flag;
    }



}
