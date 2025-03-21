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
 * @description: 阶段阈值处理类  最小级别
 * @author: Lvyp
 * @create: 2023/11/02 20:13
 */
@Component("cpuStageThreeFilterHandler")
public class CpuStageThreeFilterHandler extends IFilterHandler<CollectCpuEntity> {

    @Resource
    private IEventInfoManagerService eventInfoChangeManagerService;
    @Resource
    private ThresholdManager thresholdManager;
    @Override
    public boolean handler(CollectCpuEntity info) {
        AppLogUtils.buildLogInfo(LogFunctionEnum.DATA_PROCESS_SINGLE, "CPU三阶阈值处理类", info.getAssetIp());
        ChangeInfo changeInfo = info.getMaps().get(StatusInfoChangeTypeEnum.status_CPUState.getCode());

        String eventRedisKey = StatusInfoChangeTypeEnum.event_CPU_sectionThree.getCode();
        String eventMapKey = info.getAssetIp() + "_" + info.getAssetId();

        String redisThresholdKey = thresholdManager.getThresholdRedisKey(info.getAssetId(),info.getAssetIp());
        String thresholdMapKey = thresholdManager.getThresholdMapKey(StatusInfoChangeTypeEnum.event_CPU_sectionThree.getCode(),StatusInfoChangeTypeEnum.SECTION_THREE.getCode(),"");

        ThresholdBaseEntity threshold = thresholdManager.getThresholdValue(StatusInfoChangeTypeEnum.event_CPU_normal.getCode(), info.getAssetId(), null);
        if (threshold.threeLevelIsNull()) {
            IEvent event = eventInfoChangeManagerService.creatRecoveryThresholdEvent(info.getAssetId(), changeInfo, eventRedisKey, eventMapKey, redisThresholdKey, thresholdMapKey);
            if (event != null) {
                this.dispatureEvent(event);
            }
            return true;
        }

        boolean thresholdFlag = eventInfoChangeManagerService.infoIschange(redisThresholdKey, thresholdMapKey, threshold.getThreeLevelValue());
        if (thresholdFlag) {
            this.addThresholdStatus(redisThresholdKey,thresholdMapKey, threshold.getBaseValue(), info);
        }

        //如果性能数据有变动或者阈值有变动,则需要重新推送事件信息
        if (changeInfo.getIsChange() || thresholdFlag) {
            boolean compare = threshold.getThreeLevelValue() < (Double) changeInfo.getValue();
            Integer status = compare ? EventLevelEnum.ABNORMAL.getCode() : EventLevelEnum.NORMAL.getCode();
            String keyWord = status == EventLevelEnum.NORMAL.getCode() ? "" : "超过";

            AlarmTempReq alarmTempReq = new AlarmTempReq();
            alarmTempReq.setOrgMsg(String.format(StatusInfoChangeTypeEnum.event_CPU_sectionThree.getDescr(), changeInfo.getValue(), keyWord, threshold.getThreeLevelValue()));
            alarmTempReq.setCollectValue( changeInfo.getValue()+"%");
            alarmTempReq.setThresholdValue(threshold.getThreeLevelValue()+"%");
            alarmTempReq.setFlag("CPU");
            //添加状态监控（设备监控的事件信息是否正常）
            this.addEventStatus(StatusInfoChangeTypeEnum.event_CPU_sectionThree.getCode(),StatusInfoChangeTypeEnum.SECTION_THREE_VAL.getCode(),"", status, info, changeInfo);
            IEvent event = eventInfoChangeManagerService.creatChangeEvent(info.getAssetId(), changeInfo, eventRedisKey, eventMapKey, status,alarmTempReq);
            if (event != null) {
                //被事件信息截取
                changeInfo.setIsEvent(true);

                event.setDescStr(String.format(StatusInfoChangeTypeEnum.event_CPU_sectionThree.getDescr(), changeInfo.getValue(), keyWord, threshold.getThreeLevelValue()));
                this.dispatureEvent(event);
            }
        }

        return true;
    }

    @Override
    public boolean isNeedNexthandle(Boolean flag) {
        //如果需要下层处理需要返回true
        return true;
    }

}
