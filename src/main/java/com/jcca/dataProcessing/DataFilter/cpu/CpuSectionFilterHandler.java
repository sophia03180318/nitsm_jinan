package com.jcca.dataProcessing.DataFilter.cpu;

import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.utils.AppLogUtils;
import com.jcca.dataProcessing.Entity.ChangeInfo;
import com.jcca.dataProcessing.Entity.CollectCpuEntity;
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
 * @description: CPU区间阈值 大于最大或小于最小
 * @author: Lvyp
 * @create: 2023/11/02 19:59
 */
@Component("cpuSectionFilterHandler")
public class CpuSectionFilterHandler extends IFilterHandler<CollectCpuEntity> {

    @Resource
    private IEventInfoManagerService eventInfoChangeManagerService;
    @Resource
    private ThresholdManager thresholdManager;

    @Override
    public boolean handler(CollectCpuEntity info) {
        AppLogUtils.buildLogInfo(LogFunctionEnum.DATA_PROCESS_SINGLE, "CPU区间阈值处理类", info.getAssetIp());
        ChangeInfo changeInfo = info.getMaps().get(StatusInfoChangeTypeEnum.status_CPUState.getCode());

        String eventRedisKey = StatusInfoChangeTypeEnum.event_CPU_section.getCode();
        String eventMapKey = info.getAssetIp() + "_" + info.getAssetId();

        String redisThresholdKey = thresholdManager.getThresholdRedisKey(info.getAssetId(),info.getAssetIp());
        String thresholdMapKey = thresholdManager.getThresholdMapKey(StatusInfoChangeTypeEnum.event_CPU_section.getCode(),StatusInfoChangeTypeEnum.SECTION.getCode(),"");

        ThresholdBaseEntity threshold = thresholdManager.getThresholdValue(StatusInfoChangeTypeEnum.event_CPU_normal.getCode(), info.getAssetId(), null);
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
            alarmTempReq.setOrgMsg(String.format(StatusInfoChangeTypeEnum.event_CPU_section.getDescr(), changeInfo.getValue(), keyWord, threshold.getMinValue(), threshold.getMaxValue()));
            alarmTempReq.setCollectValue( changeInfo.getValue()+"%");
            alarmTempReq.setThresholdValue(threshold.getMinValue()+"% - "+threshold.getMaxValue()+"%");
            alarmTempReq.setFlag("CPU");
            //添加状态监控（设备监控的事件信息是否正常）
            this.addEventStatus(StatusInfoChangeTypeEnum.event_CPU_section.getCode(),StatusInfoChangeTypeEnum.SECTION_VAL.getCode(),"", status, info, changeInfo);
            IEvent event = eventInfoChangeManagerService.creatChangeEvent(info.getAssetId(), changeInfo, eventRedisKey, eventMapKey, status,alarmTempReq,info.getInspectRecordId());
            if (event != null) {
                //被事件信息截取
                event.setDescStr(String.format(StatusInfoChangeTypeEnum.event_CPU_section.getDescr(), changeInfo.getValue(), keyWord, threshold.getMinValue(), threshold.getMaxValue()));
                changeInfo.setIsEvent(true);
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
