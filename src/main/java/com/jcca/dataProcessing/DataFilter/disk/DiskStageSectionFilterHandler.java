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
 * @description: 阶段阈值处理类  阶段阈值
 * @author: Lvyp
 * @create: 2023/11/03 20:13
 */
@Component("diskStageSectionFilterHandler")
public class DiskStageSectionFilterHandler extends IFilterHandler<CollectDiskEntity> {

    @Resource
    private IEventInfoManagerService eventInfoChangeManagerService;
    @Resource
    private ThresholdManager thresholdManager;

    @Override
    public boolean handler(CollectDiskEntity info) {
        AppLogUtils.buildLogInfo(LogFunctionEnum.DATA_PROCESS_SINGLE, "磁盘区间阈值过滤处理类", info.getAssetIp());
        ChangeInfo changeInfo = info.getMaps().get(info.getName());

        String eventRedisKey = StatusInfoChangeTypeEnum.event_disk_section.getCode();
        String eventMapKey = info.getAssetIp() + "_" + info.getAssetId() + "_" + info.getName();

        String redisThresholdKey =thresholdManager.getThresholdRedisKey(info.getAssetId(),info.getAssetIp());
        String thresholdMapKey = thresholdManager.getThresholdMapKey(StatusInfoChangeTypeEnum.event_disk_section.getCode(),StatusInfoChangeTypeEnum.SECTION.getCode(),info.getName());

        ThresholdBaseEntity threshold = thresholdManager.getThresholdValue(StatusInfoChangeTypeEnum.event_disk_normal.getCode(), info.getAssetId(), info.getName());
        if (threshold.sectionValueIsNull()) {
            IEvent event = eventInfoChangeManagerService.creatRecoveryThresholdEvent(info.getAssetId(), changeInfo, eventRedisKey, eventMapKey, redisThresholdKey, thresholdMapKey,info.getInspectRecordId());
            if (event != null) {
                this.dispatureEvent(event);
            }
            return true;
        }

        String thresholdValue = threshold.getMaxValue() + "_" + threshold.getMinValue();
        boolean thresholdFlag = eventInfoChangeManagerService.infoIschange(info.getInspectRecordId(),redisThresholdKey, thresholdMapKey, thresholdValue);
        if (thresholdFlag) {
            this.addThresholdStatus(redisThresholdKey,thresholdMapKey, thresholdValue, info);
        }

        //如果性能数据有变动或者阈值有变动,则需要重新推送事件信息
        if (changeInfo.getIsChange() || thresholdFlag) {
            //阈值范围告警：高于最高或者低于最低产生告警
            boolean compare = threshold.getMaxValue() < (Double) changeInfo.getValue() || threshold.getMinValue() > (Double) changeInfo.getValue();
            Integer status = compare ? EventLevelEnum.ABNORMAL.getCode() : EventLevelEnum.NORMAL.getCode();

            String keyWord = status == EventLevelEnum.NORMAL.getCode() ? "" : "不在";
            AlarmTempReq alarmTempReq = new AlarmTempReq();
            alarmTempReq.setOrgMsg(String.format(StatusInfoChangeTypeEnum.event_disk_section.getDescr(), info.getName(), changeInfo.getValue(), keyWord , threshold.getMinValue(), threshold.getMaxValue()));
            alarmTempReq.setCollectValue(changeInfo.getValue()+"%");
            alarmTempReq.setThresholdValue( threshold.getMinValue()+"% - "+ threshold.getMaxValue()+"%");
            alarmTempReq.setFlag(info.getName());
            //添加状态监控（设备监控的事件信息是否正常）
            this.addEventStatus(StatusInfoChangeTypeEnum.event_disk_section.getCode(),StatusInfoChangeTypeEnum.SECTION_VAL.getCode(),info.getName(), status, info, changeInfo);
            IEvent event = eventInfoChangeManagerService.creatChangeEvent(info.getAssetId(), changeInfo, eventRedisKey, eventMapKey, status,alarmTempReq,info.getInspectRecordId());
            if (event != null) {
                //被事件信息截取
                changeInfo.setIsEvent(true);

                event.setDescStr(String.format(StatusInfoChangeTypeEnum.event_disk_section.getDescr(), info.getName(), changeInfo.getValue(), keyWord , threshold.getMinValue(), threshold.getMaxValue()));
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
