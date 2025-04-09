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
import com.jcca.web.event.enums.EventLevelEnum;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * @author Zhaozheng
 * @description TODO 普通单活判定事件
 * @className ProcessStateFilterHandler
 * @date 2023/10/27 9:57
 * @since 2.1.0.0
 */
@Component("processGroupSingleStateFilterHandler")
public class ProcessGroupSingleStateFilterHandler extends IFilterHandler<ProcessGroupEntity> {


    @Resource
    private IEventInfoManagerService eventInfoChangeManagerService;

    @Override
    public boolean handler(ProcessGroupEntity req) {
        AppLogUtils.buildLogInfo(LogFunctionEnum.DATA_PROCESS_SINGLE, "双机单断进程处理", req.getAssetIp());
        List<ProcessAlarmQueueEntity> queueObj = req.getQueueObj();
        if (queueObj.size() == 1) {
            ProcessAlarmQueueEntity info = queueObj.get(0);
            if (!StringUtils.isEmpty(info.getProcessChange())) {
                return true;
            }

            String redisKey = req.getAssetIp() + ":" + req.getAssetId() + ":" + StatusInfoChangeTypeEnum.status_process_status.getCode();
            boolean compare = info.getProcessStatus();
            String mapKey = info.getProcessName();

            boolean flag = eventInfoChangeManagerService.infoIschange(redisKey, mapKey, compare);
            if (flag) {
                ChangeInfo changeInfo = new ChangeInfo();
                changeInfo.setValue(compare);
                changeInfo.setRedisKey(redisKey);
                changeInfo.setMapKey(mapKey);
                changeInfo.setCollectTime(new Date());
                req.getMaps().put(mapKey, changeInfo);

                Integer status = compare ? EventLevelEnum.NORMAL.getCode() : EventLevelEnum.ABNORMAL.getCode();
                String eventRedisKey = StatusInfoChangeTypeEnum.event_process_status.getCode();
                String eventMapKey = req.getAssetIp() + ":" + req.getAssetId() + ":" + info.getProcessName();
                AlarmTempReq alarmTempReq = new AlarmTempReq();
                if (status == EventLevelEnum.NORMAL.getCode()) {
                    alarmTempReq.setOrgMsg(" 恢复的进程ID:" + info.getProcessId() + " " + String.format(StatusInfoChangeTypeEnum.event_process_status.getDescr(), info.getProcessName(), info.getAlias(), info.getProcessId()));
                } else {
                    alarmTempReq.setOrgMsg(String.format(StatusInfoChangeTypeEnum.event_process_status.getDescr(), info.getProcessName(), info.getProcessId()));
                }
                alarmTempReq.setCollectValue(changeInfo.getValue().toString());
                alarmTempReq.setFlag(info.getProcessId());
                this.addEventStatus(StatusInfoChangeTypeEnum.event_process_status.getCode(), StatusInfoChangeTypeEnum.STATUS.getCode(), info.getProcessName(), status, req, changeInfo);
                IEvent event = eventInfoChangeManagerService.creatChangeEvent(req.getAssetId(), changeInfo, eventRedisKey, eventMapKey, status, alarmTempReq);
                if (event != null) {
                    //被事件信息截取
                    changeInfo.setIsEvent(true);
                    //进程恢复
                    if (status == EventLevelEnum.NORMAL.getCode()) {
                        event.setRecoveryProcessIdDescr(" 恢复的进程ID:" + info.getProcessId() + " ");
                    }
                    event.setDescStr(String.format(StatusInfoChangeTypeEnum.event_process_status.getDescr(), info.getProcessName(), info.getAlias(), info.getProcessId()));
                    this.dispatureEvent(event);
                }
            }
        }


        return true;
    }

    @Override
    public boolean isNeedNexthandle(Boolean flag) {
        return flag;
    }

}
