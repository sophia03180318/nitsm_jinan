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
 * @description: 阶段阈值处理类  最高级别
 * @author: Lvyp
 * @create: 2023/11/02 20:13
 */
@Component("memoryStageOneFilterHandler")
public class MemoryStageOneFilterHandler extends IFilterHandler<CollectMemoryEntity> {

    @Resource
    private IEventInfoManagerService eventInfoChangeManagerService;
    @Resource
    private ThresholdManager thresholdManager;

    @Override
    public boolean handler(CollectMemoryEntity info) {
        AppLogUtils.buildLogInfo(LogFunctionEnum.DATA_PROCESS_SINGLE, "内存一阶阈值信息过滤处理类", info.getAssetIp());
        ChangeInfo changeInfo = info.getMaps().get(StatusInfoChangeTypeEnum.status_memoryState.getCode());

        String eventRedisKey = StatusInfoChangeTypeEnum.event_memory_sectionOne.getCode();
        String eventMapKey = info.getAssetIp() + "_" + info.getAssetId();

        String redisThresholdKey =thresholdManager.getThresholdRedisKey(info.getAssetId(),info.getAssetIp());
        String thresholdMapKey = thresholdManager.getThresholdMapKey(StatusInfoChangeTypeEnum.event_memory_sectionOne.getCode(),StatusInfoChangeTypeEnum.SECTION_ONE.getCode(),null);

        ThresholdBaseEntity threshold = thresholdManager.getThresholdValue(StatusInfoChangeTypeEnum.event_memory_normal.getCode(), info.getAssetId(), null);
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
            String keyWord=status.equals(EventLevelEnum.NORMAL.getCode())?"":"超过";
            String descStr = String.format(StatusInfoChangeTypeEnum.event_memory_sectionOne.getDescr(), changeInfo.getValue(), keyWord, threshold.getOneLevelValue());

            AlarmTempReq tempReq = new AlarmTempReq();
            tempReq.setOrgMsg(descStr);
            tempReq.setThresholdValue(threshold.getOneLevelValue() +"%");
            tempReq.setCollectValue(changeInfo.getValue()+"%");
            tempReq.setFlag("内存");

            //添加状态监控（设备监控的事件信息是否正常）
            this.addEventStatus(StatusInfoChangeTypeEnum.event_memory_sectionOne.getCode(),StatusInfoChangeTypeEnum.SECTION_ONE_VAL.getCode(),null, status, info, changeInfo);
            IEvent event = eventInfoChangeManagerService.creatChangeEvent(info.getAssetId(), changeInfo, eventRedisKey, eventMapKey, status,tempReq,info.getInspectRecordId());
            if (event != null) {
                //被事件信息截取
                changeInfo.setIsEvent(true);
                event.setDescStr(descStr);

                this.dispatureEvent(event);
            }

        }
        //一阶、二阶、三阶阈值告警信息，命中哪一个就是哪一个不会再命中其他的处理类
        if (compare) {//触发异常时，不需要进入二阶
            return false;
        }
        return true;
    }

    @Override
    public boolean isNeedNexthandle(Boolean flag) {
        return flag;
    }


}
