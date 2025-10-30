package com.jcca.dataProcessing.DataFilter.process;

import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.utils.AppLogUtils;
import com.jcca.common.utils.AppMathUtil;
import com.jcca.dataProcessing.Entity.ChangeInfo;
import com.jcca.dataProcessing.Entity.CollectProcessEntity;
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
 * @description TODO 进程CPU过滤处理类
 * @className ProcessCpuFilterHandler
 * @date 2023/10/27 9:58
 * @since 2.1.0.0
 */
@Component("processCpuFilterHandler")
public class ProcessCpuFilterHandler extends IFilterHandler<CollectProcessEntity> {

    @Resource
    private IEventInfoManagerService eventInfoChangeManagerService;
    @Resource
    private ThresholdManager thresholdManager;



    @Override
    public boolean handler(CollectProcessEntity info) {
        if (!info.getStatus()) {
            return true;
        }
        AppLogUtils.buildLogInfo(LogFunctionEnum.DATA_PROCESS_SINGLE, "进程CPU处理", info.getAssetIp());
        String redisKey = info.getAssetIp() + ":" + info.getAssetId() + ":" + StatusInfoChangeTypeEnum.status_process.getCode()+":"+info.getName();
        String mapKey = StatusInfoChangeTypeEnum.status_process_cpu.getCode();
        //判断数据是否有变化
        boolean flag = eventInfoChangeManagerService.infoIschange(info.getInspectRecordId(),redisKey, mapKey, info.getCpuRate());
        ChangeInfo changeInfo = new ChangeInfo();
        changeInfo.setValue(info.getCpuRate());
        changeInfo.setRedisKey(redisKey);
        changeInfo.setIsChange(flag);
        changeInfo.setMapKey(mapKey);
        changeInfo.setCollectTime(new Date());
        info.getMaps().put(mapKey, changeInfo);

        String eventRedisKey = StatusInfoChangeTypeEnum.event_process_cpu.getCode();
        String eventMapKey = info.getAssetIp() + "_" + info.getAssetId() + "_" + info.getName();

        String redisThresholdKey =thresholdManager.getThresholdRedisKey(info.getAssetId(),info.getAssetIp());
        String thresholdMapKey = thresholdManager.getThresholdMapKey(StatusInfoChangeTypeEnum.event_process_cpu.getCode(),StatusInfoChangeTypeEnum.CPU.getCode(), info.getName());

        ThresholdBaseEntity threshold = thresholdManager.getThresholdValue(StatusInfoChangeTypeEnum.event_process_cpu.getCode(), info.getAssetId(), info.getName());
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
        if (changeInfo.getIsChange() || thresholdFlag) {
            Boolean compare = AppMathUtil.compare(info.getCpuRate() + "", threshold.getBaseValue() + "");
            Integer status = compare ? EventLevelEnum.ABNORMAL.getCode() : EventLevelEnum.NORMAL.getCode();
            String keyWord=status==EventLevelEnum.NORMAL.getCode()?"":"超过";
            AlarmTempReq alarmTempReq = new AlarmTempReq();
            alarmTempReq.setOrgMsg(String.format(StatusInfoChangeTypeEnum.event_process_cpu.getDescr(), info.getName(), info.getProcessId(), changeInfo.getValue(), keyWord, threshold.getBaseValue()));
            alarmTempReq.setCollectValue(changeInfo.getValue()+"%");
            alarmTempReq.setThresholdValue(threshold.getBaseValue()+"%");
            alarmTempReq.setFlag( info.getName());
            this.addEventStatus(StatusInfoChangeTypeEnum.event_process_cpu.getCode(),StatusInfoChangeTypeEnum.CPU_VAL.getCode(), info.getName(), status, info, changeInfo);
            IEvent event = eventInfoChangeManagerService.creatChangeEvent(info.getAssetId(), changeInfo, eventRedisKey, eventMapKey, status,alarmTempReq,info.getInspectRecordId(),info.getVersion());
            if (event != null) {
                //被事件信息截取
                changeInfo.setIsEvent(true);

                event.setDescStr(String.format(StatusInfoChangeTypeEnum.event_process_cpu.getDescr(), info.getName(), info.getProcessId(), changeInfo.getValue(), keyWord, threshold.getBaseValue()));
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
