package com.jcca.dataProcessing.DataFilter.disk;

import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.utils.AppLogUtils;
import com.jcca.dataProcessing.Entity.ChangeInfo;
import com.jcca.dataProcessing.Entity.CollectDiskEntity;
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
 * @description: 阶段阈值处理类  中间级别
 * @author: Lvyp
 * @create: 2023/11/02 20:13
 */
@Component("diskStageTwoFilterHandler")
public class DiskStageTwoFilterHandler extends IFilterHandler<CollectDiskEntity> {

    @Resource
    private IEventInfoManagerService eventInfoChangeManagerService;
    @Resource
    private ThresholdManager thresholdManager;

    @Override
    public boolean handler(CollectDiskEntity info) {
        AppLogUtils.buildLogInfo(LogFunctionEnum.DATA_PROCESS_SINGLE, "磁盘二阶阈值过滤处理类", info.getAssetIp());
        ChangeInfo changeInfo = info.getMaps().get(info.getName());
        String eventRedisKey = StatusInfoChangeTypeEnum.event_disk_sectionTwo.getCode();
        String eventMapKey = info.getAssetIp() + "_" + info.getAssetId() + "_" + info.getName();

        String redisThresholdKey =thresholdManager.getThresholdRedisKey(info.getAssetId(),info.getAssetIp());
        String thresholdMapKey = thresholdManager.getThresholdMapKey(StatusInfoChangeTypeEnum.event_disk_sectionTwo.getCode(),StatusInfoChangeTypeEnum.SECTION_TWO.getCode(),info.getName());

        ThresholdBaseEntity threshold = thresholdManager.getThresholdValue(StatusInfoChangeTypeEnum.event_disk_normal.getCode(), info.getAssetId(), info.getName());
        if (threshold.twoLevelIsNull()) {
            IEvent event = eventInfoChangeManagerService.creatRecoveryThresholdEvent(info.getAssetId(), changeInfo, eventRedisKey, eventMapKey, redisThresholdKey, thresholdMapKey,info.getInspectRecordId());
            if (event != null) {
                this.dispatureEvent(event);
            }
            return true;
        }

        boolean thresholdFlag = eventInfoChangeManagerService.infoIschange(info.getInspectRecordId(),redisThresholdKey, thresholdMapKey, threshold.getTwoLevelValue());
        if (thresholdFlag) {
            this.addThresholdStatus(redisThresholdKey,thresholdMapKey, threshold.getBaseValue(), info);
        }
        boolean compare = threshold.getTwoLevelValue() < (Double) changeInfo.getValue();
        //如果性能数据有变动或者阈值有变动,则需要重新推送事件信息
        if (changeInfo.getIsChange() || thresholdFlag) {

            Integer status = compare ? EventLevelEnum.ABNORMAL.getCode() : EventLevelEnum.NORMAL.getCode();
            String keyWord = status.equals(EventLevelEnum.NORMAL.getCode()) ? "" : "超过";
            AlarmTempReq alarmTempReq = new AlarmTempReq();
            alarmTempReq.setOrgMsg(String.format(StatusInfoChangeTypeEnum.event_disk_sectionTwo.getDescr(), info.getName(), changeInfo.getValue(), keyWord, threshold.getTwoLevelValue()));
            alarmTempReq.setCollectValue(changeInfo.getValue()+"%");
            alarmTempReq.setThresholdValue(threshold.getTwoLevelValue()+"%");
            alarmTempReq.setFlag(info.getName());
            IEvent event = eventInfoChangeManagerService.creatChangeEvent(info.getAssetId(), changeInfo, eventRedisKey, eventMapKey, status,alarmTempReq,info.getInspectRecordId(),info.getVersion());
            //添加状态监控（设备监控的事件信息是否正常）
            this.addEventStatus(StatusInfoChangeTypeEnum.event_disk_sectionTwo.getCode(),StatusInfoChangeTypeEnum.SECTION_TWO_VAL.getCode(),info.getName(), status, info, changeInfo);
            if (event != null) {
                //被事件信息截取
                changeInfo.setIsEvent(true);
                event.setDescStr(String.format(StatusInfoChangeTypeEnum.event_disk_sectionTwo.getDescr(), info.getName(), changeInfo.getValue(), keyWord, threshold.getTwoLevelValue()));
                this.dispatureEvent(event);
            }

        }
        if (compare) {
            return false;
        }

        return true;

    }

    @Override
    public boolean isNeedNextHandle(Boolean flag) {
        return flag;
    }

}
