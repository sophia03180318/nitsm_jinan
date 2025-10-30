package com.jcca.dataProcessing.DataFilter.process;

import com.jcca.dataProcessing.Entity.ChangeInfo;
import com.jcca.dataProcessing.Entity.ProcessAlarmQueueEntity;
import com.jcca.dataProcessing.Entity.ProcessGroupEntity;
import com.jcca.dataProcessing.enums.StatusInfoChangeTypeEnum;
import com.jcca.dataProcessing.manager.IEventInfoManagerService;
import com.jcca.dataProcessing.manager.bean.AlarmTempReq;
import com.jcca.dataProcessing.support.IEvent;
import com.jcca.dataProcessing.support.IFilterHandler;
import com.jcca.web.asset.utils.enums.ProcessHostModeEnum;
import com.jcca.web.event.enums.EventLevelEnum;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Zhaozheng
 * @description TODO 进程双击双活判定事件  需要在外面判定是不是双机的
 * @className ProcessStateFilterHandler
 * @date 2023/10/27 9:57
 * @since 2.1.0.0
 */
@Component("ProcessGroupDoubleStateFilterHandler")
public class ProcessGroupDoubleStateFilterHandler extends IFilterHandler<ProcessGroupEntity> {

    @Resource
    private IEventInfoManagerService eventInfoChangeManagerService;

    @Override
    public boolean handler(ProcessGroupEntity entity) {
        List<ProcessAlarmQueueEntity> queueObj = entity.getQueueObj();

        List<ProcessAlarmQueueEntity> normalAsset = new ArrayList<ProcessAlarmQueueEntity>();
        List<ProcessAlarmQueueEntity> errorAsset = new ArrayList<ProcessAlarmQueueEntity>();

        for (ProcessAlarmQueueEntity process : queueObj) {
            if(!ProcessHostModeEnum.DOUBLE_HOST_DOUBLE_LIVE.getCode().equals(process.getHostMode())){
                return true;
            }
            if (process.getProcessStatus()) {
                normalAsset.add(process);
            } else {
                errorAsset.add(process);
            }
        }


        for (ProcessAlarmQueueEntity info : queueObj) {
            String redisKey = info.getAssetIp() + ":" + info.getAssetId() + ":" + StatusInfoChangeTypeEnum.status_process_status.getCode();
            String mapKey = info.getProcessName() + "_all_down";
            Boolean processStatus = info.getProcessStatus();
            //组进程全部掉线事件Flag 全部掉线则value为true 否则为false
            boolean flag = eventInfoChangeManagerService.infoIschange(info.getInspectRecordId(),redisKey, mapKey, normalAsset.isEmpty());
            if (flag) {
                ChangeInfo changeInfo = new ChangeInfo();
                changeInfo.setValue(normalAsset.isEmpty());
                changeInfo.setRedisKey(redisKey);
                changeInfo.setMapKey(mapKey);
                changeInfo.setCollectTime(new Date());
                entity.getMaps().put(mapKey, changeInfo);

                String eventRedisKey = StatusInfoChangeTypeEnum.event_process_all_down.getCode();
                Integer status = normalAsset.isEmpty() ? EventLevelEnum.ABNORMAL.getCode() : EventLevelEnum.NORMAL.getCode();
                String eventMapKey = info.getAssetIp() + "_" + info.getAssetId() + "_" + info.getProcessName();
                AlarmTempReq alarmTempReq = new AlarmTempReq();
                alarmTempReq.setOrgMsg(String.format(StatusInfoChangeTypeEnum.event_process_all_down.getDescr(), info.getProcessName(), info.getAlias(), info.getProcessId()));
                alarmTempReq.setCollectValue(changeInfo.getValue().toString());
                alarmTempReq.setFlag(info.getProcessId());
                this.addEventStatus(StatusInfoChangeTypeEnum.event_process_all_down.getCode(), StatusInfoChangeTypeEnum.STATUS.getCode(), info.getProcessName(), status, entity, changeInfo);
                IEvent event = eventInfoChangeManagerService.creatChangeEvent(info.getAssetId(), changeInfo, eventRedisKey, eventMapKey, status, alarmTempReq,info.getInspectRecordId(),info.getVersion());
                if (event != null) {
                    //被事件信息截取
                    changeInfo.setIsEvent(true);
                    event.setDescStr(String.format(StatusInfoChangeTypeEnum.event_process_all_down.getDescr(), info.getProcessName(), info.getAlias(), info.getProcessId()));
                    this.dispatureEvent(event);
                }

            }

            String mapKey2 = info.getProcessName() + "_other_down_" + info.getProcessName();
            //组进程部分掉线事件 Flag errorAsset 有值则是异常 无值则是正常 部分掉线则value为true 否则为false
            boolean flag1 = eventInfoChangeManagerService.infoIschange(info.getInspectRecordId(),redisKey, mapKey2, info.getProcessStatus());
            if (flag1 && !normalAsset.isEmpty()) {
                ChangeInfo changeInfo = new ChangeInfo();
                changeInfo.setValue(info.getProcessStatus());
                changeInfo.setRedisKey(redisKey);
                changeInfo.setCollectTime(new Date());
                changeInfo.setMapKey(mapKey2);
                entity.getMaps().put(mapKey2, changeInfo);

                String eventRedisKey = StatusInfoChangeTypeEnum.event_process_other_down.getCode();
                Integer status = info.getProcessStatus() ? EventLevelEnum.NORMAL.getCode() : EventLevelEnum.ABNORMAL.getCode();
                String eventMapKey = info.getAssetIp() + "_" + info.getAssetId() + "_" + info.getProcessName();

                AlarmTempReq alarmTempReq = new AlarmTempReq();

                if (processStatus) {
                    alarmTempReq.setOrgMsg(" 恢复的进程ID:" + info.getProcessId() + " " + String.format(StatusInfoChangeTypeEnum.event_process_other_down.getDescr(), info.getProcessName(),info.getAlias(), info.getProcessId()));
                } else {
                    alarmTempReq.setOrgMsg(String.format(StatusInfoChangeTypeEnum.event_process_other_down.getDescr(), info.getProcessName(),info.getAlias(), info.getProcessId()));
                }
                alarmTempReq.setFlag(info.getProcessId());
                IEvent event = eventInfoChangeManagerService.creatChangeEvent(info.getAssetId(), changeInfo, eventRedisKey, eventMapKey, status, alarmTempReq,info.getInspectRecordId(),info.getVersion());
                if (event != null) {
                    //被事件信息截取
                    changeInfo.setIsEvent(true);
                    if (processStatus) {
                        event.setRecoveryProcessIdDescr(" 恢复的进程ID:" + info.getProcessId() + " ");
                        event.setDescStr(String.format(StatusInfoChangeTypeEnum.event_process_other_down.getDescr(), info.getProcessName(), info.getAlias(),info.getProcessId()));
                        this.dispatureEvent(event);
                    } else {
                        event.setDescStr(String.format(StatusInfoChangeTypeEnum.event_process_other_down.getDescr(), info.getProcessName(),info.getAlias(), info.getProcessId()));
                        this.dispatureEvent(event);
                    }
                }
            }

        }

        return false;
    }

    @Override
    public boolean isNeedNextHandle(Boolean flag) {
        return flag;
    }

}
