package com.jcca.dataProcessing.DataFilter.process;

import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.utils.AppLogUtils;
import com.jcca.dataProcessing.Entity.ChangeInfo;
import com.jcca.dataProcessing.Entity.ProcessAlarmQueueEntity;
import com.jcca.dataProcessing.Entity.ProcessGroupEntity;
import com.jcca.dataProcessing.enums.StatusInfoChangeTypeEnum;
import com.jcca.dataProcessing.manager.IEventInfoManagerService;
import com.jcca.dataProcessing.manager.bean.AlarmTempReq;
import com.jcca.dataProcessing.support.IEvent;
import com.jcca.dataProcessing.support.IFilterHandler;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.asset.service.AssetService;
import com.jcca.web.event.enums.EventLevelEnum;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * @author: hhw
 * @description: ProcessChangeFilterHandler主要是用来处理进程双机单活时候的进程切换告警
 * @date: 2025-03-11  10:11
 * @since: 2.1.4.0
 */
@Component("processChangeFilterHandler")
public class ProcessChangeFilterHandler extends IFilterHandler<ProcessGroupEntity> {

    @Resource
    private IEventInfoManagerService eventInfoChangeManagerService;
    @Resource
    private AssetService assetService;

    @Override
    public boolean handler(ProcessGroupEntity entity) throws Exception {
        AppLogUtils.buildLogInfo(LogFunctionEnum.DATA_PROCESS_SINGLE, "双机单活进程切换", entity.getAssetIp());
        List<ProcessAlarmQueueEntity> queueObj = entity.getQueueObj();

        String assetName = "";
        String assetIP = "";
        for (ProcessAlarmQueueEntity processAlarmQueueEntity : queueObj) {
            if (processAlarmQueueEntity.getProcessStatus()) {
                Asset asset = assetService.getById(processAlarmQueueEntity.getAssetId());
                assetName = asset.getName();
                assetIP = asset.getIp();
            }
        }

        if (StringUtils.isEmpty(assetName)) {
            return true;
        }

        for (ProcessAlarmQueueEntity info : queueObj) {
            String processChange = info.getProcessChange();
            if (StringUtils.isEmpty(processChange)) {
                continue;
            }
            entity.setAssetId(info.getAssetId());

            AppLogUtils.buildLogInfo(LogFunctionEnum.DATA_PROCESS_CHANGE, info.getProcessName() + "进程切换告警处理", info);

            String redisKey = info.getAssetIp() + ":" + info.getAssetId() + ":" + StatusInfoChangeTypeEnum.status_process_status.getCode();
            String mapKey = info.getProcessName() + "_processChange";
            Boolean processStatus = info.getProcessStatus();
            Boolean flag = eventInfoChangeManagerService.infoIschangeFirst(info.getInspectRecordId(),redisKey, mapKey, processStatus);
            AppLogUtils.buildLogInfo(LogFunctionEnum.DATA_PROCESS_CHANGE, info.getProcessName()+info.getAssetIp() + "进程切换告警处理判定", flag);
            if (flag == null || flag) {
                ChangeInfo changeInfo = new ChangeInfo();
                changeInfo.setValue(processStatus);
                changeInfo.setRedisKey(redisKey);
                changeInfo.setMapKey(mapKey);
                changeInfo.setCollectTime(new Date());
                entity.getMaps().put(mapKey, changeInfo);


                AlarmTempReq alarmTempReq = new AlarmTempReq();
                alarmTempReq.setOrgMsg(String.format(StatusInfoChangeTypeEnum.event_process_once.getDescr(),
                        info.getProcessName(), info.getAlias(), assetName + "(" + assetIP + ")"));
                alarmTempReq.setCollectValue(changeInfo.getValue().toString());
                alarmTempReq.setFlag(info.getProcessId());
                this.addEventStatus(StatusInfoChangeTypeEnum.event_process_once.getCode(), StatusInfoChangeTypeEnum.STATUS.getCode(),
                        info.getProcessName(), EventLevelEnum.ABNORMAL.getCode(), entity, changeInfo);

                String eventRedisKey = StatusInfoChangeTypeEnum.event_process_once.getCode();
                String eventMapKey = info.getAssetIp() + "_" + info.getAssetId() + "_" + info.getProcessName();
                IEvent event = eventInfoChangeManagerService.creatChangeEvent(info.getAssetId(), changeInfo, eventRedisKey,
                        eventMapKey, EventLevelEnum.ABNORMAL.getCode(), alarmTempReq,info.getInspectRecordId(),info.getVersion());

                AppLogUtils.buildLogInfo(LogFunctionEnum.DATA_PROCESS_CHANGE, info.getProcessName()+info.getAssetIp() + "进程切换告警event判定", Objects.nonNull(event));
                if (event != null) {
                    changeInfo.setIsEvent(true);
                    event.setDescStr(String.format(StatusInfoChangeTypeEnum.event_process_once.getDescr(),
                            info.getProcessName(), info.getAlias(), assetName + "(" + assetIP + ")"));
                    this.dispatureEvent(event);
                    return false;
                }
            }
        }
        return true;
    }


    @Override
    public boolean isNeedNextHandle(Boolean flag) {
        return flag;
    }
}
