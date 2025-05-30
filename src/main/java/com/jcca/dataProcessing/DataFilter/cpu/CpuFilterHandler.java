package com.jcca.dataProcessing.DataFilter.cpu;

import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.utils.AppLogUtils;
import com.jcca.common.utils.AppMathUtil;
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
import java.util.Date;

/**
 * @author Zhaozheng
 * @description TODO CPU 普通阈值变动处理类
 * @className CpuFitlerHandler
 * @date 2023/10/27 9:26
 * @since 2.1.0.0
 */
@Component("cpuFilterHandler")
public class CpuFilterHandler extends IFilterHandler<CollectCpuEntity> {

    @Resource
    private IEventInfoManagerService eventInfoChangeManagerService;
    @Resource
    private ThresholdManager thresholdManager;

    @Override
    public boolean handler(CollectCpuEntity info) {
        AppLogUtils.buildLogInfo(LogFunctionEnum.DATA_PROCESS_SINGLE, "CPU普通阈值变动处理类", info.getAssetIp());
        String redisKey = info.getAssetIp() + ":" + info.getAssetId() + ":" + StatusInfoChangeTypeEnum.status.getCode();
        String mapKey = StatusInfoChangeTypeEnum.status_CPUState.getCode();
        //判断数据是否有变化
        boolean flag = eventInfoChangeManagerService.infoIschange(info.getInspectRecordId(),redisKey, mapKey, info.getCpuUsedRate());
        ChangeInfo changeInfo = new ChangeInfo();
        changeInfo.setValue(info.getCpuUsedRate());
        changeInfo.setRedisKey(redisKey);
        changeInfo.setMapKey(mapKey);
        changeInfo.setCollectTime(new Date(info.getCollectTime()));
        changeInfo.setIsChange(flag);
        info.getMaps().put(mapKey, changeInfo);


        String eventRedisKey = StatusInfoChangeTypeEnum.event_CPU_normal.getCode();
        String eventMapKey = info.getAssetIp() + "_" + info.getAssetId();

        String redisThresholdKey = thresholdManager.getThresholdRedisKey(info.getAssetId(), info.getAssetIp());
        String thresholdMapKey = thresholdManager.getThresholdMapKey(StatusInfoChangeTypeEnum.event_CPU_normal.getCode(), StatusInfoChangeTypeEnum.NORMAL.getCode(), "");

        ThresholdBaseEntity threshold = thresholdManager.getThresholdValue(StatusInfoChangeTypeEnum.event_CPU_normal.getCode(), info.getAssetId(), null);
        if (threshold.baseValueIsNull()) {
            IEvent event = eventInfoChangeManagerService.creatRecoveryThresholdEvent(info.getAssetId(), changeInfo, eventRedisKey, eventMapKey, redisThresholdKey, thresholdMapKey,info.getInspectRecordId());
            if (event != null) {
                this.dispatureEvent(event);
            }
            return true;
        }

        boolean thresholdFlag = eventInfoChangeManagerService.infoIschange(info.getInspectRecordId(),redisThresholdKey, thresholdMapKey, threshold.getBaseValue());
        if (thresholdFlag) {
            this.addThresholdStatus(redisThresholdKey, thresholdMapKey, threshold.getBaseValue(), info);
        }

        //如果性能数据有变动或者阈值有变动,则需要重新推送事件信息
        if (flag || thresholdFlag) {
            Boolean compare = AppMathUtil.compare(info.getCpuUsedRate() + "", threshold.getBaseValue() + "");
            Integer status = compare ? EventLevelEnum.ABNORMAL.getCode() : EventLevelEnum.NORMAL.getCode();
            String keyWord = status.equals(EventLevelEnum.NORMAL.getCode()) ? "" : "超过";
            AlarmTempReq alarmTempReq = new AlarmTempReq();
            alarmTempReq.setOrgMsg(String.format(StatusInfoChangeTypeEnum.event_CPU_normal.getDescr(), changeInfo.getValue(), keyWord, threshold.getBaseValue()));
            alarmTempReq.setCollectValue(info.getCpuUsedRate() + "%");
            alarmTempReq.setThresholdValue(threshold.getBaseValue()+"%");
            alarmTempReq.setFlag("CPU");
            //添加状态监控（设备监控的事件信息是否正常）
            this.addEventStatus(StatusInfoChangeTypeEnum.event_CPU_normal.getCode(), StatusInfoChangeTypeEnum.NORMAL_VAL.getCode(), "", status, info, changeInfo);
            IEvent event = eventInfoChangeManagerService.creatChangeEvent(info.getAssetId(), changeInfo, eventRedisKey, eventMapKey, status, alarmTempReq,info.getInspectRecordId());
            if (event != null) {
                //被事件信息截取

                event.setDescStr(String.format(StatusInfoChangeTypeEnum.event_CPU_normal.getDescr(), changeInfo.getValue(), keyWord, threshold.getBaseValue()));
                changeInfo.setIsEvent(true);
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
