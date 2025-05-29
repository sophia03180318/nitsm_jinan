package com.jcca.dataProcessing.DataFilter.memory;

import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.utils.AppLogUtils;
import com.jcca.dataProcessing.Entity.ChangeInfo;
import com.jcca.dataProcessing.Entity.CollectMemoryEntity;
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
 * @description: 区间阈值 大于最大或小于最小
 * @author: Lvyp
 * @create: 2023/11/02 19:59
 */
@Component("memorySectionFilterHandler")
public class MemorySectionFilterHandler extends IFilterHandler<CollectMemoryEntity> {


    @Resource
    private IEventInfoManagerService eventInfoChangeManagerService;
    @Resource
    private ThresholdManager thresholdManager;

    @Override
    public boolean handler(CollectMemoryEntity info) {
        AppLogUtils.buildLogInfo(LogFunctionEnum.DATA_PROCESS_SINGLE, "内存区间阈值信息过滤处理类", info.getAssetIp());

        ChangeInfo changeInfo = info.getMaps().get(StatusInfoChangeTypeEnum.status_memoryState.getCode());
        String eventRedisKey = StatusInfoChangeTypeEnum.event_memory_section.getCode();
        String eventMapKey = info.getAssetIp() + "_" + info.getAssetId();

        String redisThresholdKey =thresholdManager.getThresholdRedisKey(info.getAssetId(),info.getAssetIp());
        String thresholdMapKey = thresholdManager.getThresholdMapKey(StatusInfoChangeTypeEnum.event_memory_section.getCode(),StatusInfoChangeTypeEnum.SECTION.getCode(),null);

        ThresholdBaseEntity threshold = thresholdManager.getThresholdValue(StatusInfoChangeTypeEnum.event_memory_normal.getCode(), info.getAssetId(), null);
        if (threshold.sectionValueIsNull()) {
            IEvent event = eventInfoChangeManagerService.creatRecoveryThresholdEvent(info.getAssetId(), changeInfo, eventRedisKey, eventMapKey, redisThresholdKey, thresholdMapKey);
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
            String keyWord = status.equals(EventLevelEnum.NORMAL.getCode()) ? "" : "不在";
            String descStr = String.format(StatusInfoChangeTypeEnum.event_memory_section.getDescr(), changeInfo.getValue(), keyWord, threshold.getMinValue(), threshold.getMaxValue());

            AlarmTempReq tempReq = new AlarmTempReq();
            tempReq.setOrgMsg(descStr);
            tempReq.setThresholdValue(threshold.getMinValue()+"% - "+threshold.getMaxValue()+"%");
            tempReq.setCollectValue(changeInfo.getValue()+"%");
            tempReq.setFlag("内存");

            //添加状态监控（设备监控的事件信息是否正常）
            this.addEventStatus(StatusInfoChangeTypeEnum.event_memory_section.getCode(),StatusInfoChangeTypeEnum.SECTION_VAL.getCode(),null, status, info, changeInfo);
            IEvent event = eventInfoChangeManagerService.creatChangeEvent(info.getAssetId(), changeInfo, eventRedisKey, eventMapKey, status,tempReq);
            if (event != null) {
                //被事件信息截取
                changeInfo.setIsEvent(true);
                event.setDescStr(descStr);
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
