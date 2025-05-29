package com.jcca.dataProcessing.DataFilter.process;

import cn.hutool.core.util.StrUtil;
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
import java.util.Date;
import java.util.List;

/**
 * @author Zhaozheng
 * @description TODO 单活判定事件
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
        List<ProcessAlarmQueueEntity> queueObj = req.getQueueObj();

        //有一个正常则全部正常
        boolean status = false;
        for (ProcessAlarmQueueEntity info : queueObj) {
            if(!ProcessHostModeEnum.DOUBLE_HOST_SINGLE_LIVE.getCode().equals(info.getHostMode()) ){
                return true;
            }
            //排除切换告警
            if(StrUtil.isNotEmpty( info.getProcessChange())){
                return true;
            }
            Boolean processStatus = info.getProcessStatus();
            if(processStatus){
                status = true;
            }
        }

        for (ProcessAlarmQueueEntity info : queueObj) {
            String redisKey = info.getAssetIp() + ":" + info.getAssetId() + ":" + StatusInfoChangeTypeEnum.event_process_status.getCode();
            String mapKey = info.getProcessName();

            boolean flag = eventInfoChangeManagerService.infoIschange(info.getInspectRecordId(),redisKey, mapKey, status);
            if (flag) {
                ChangeInfo changeInfo = new ChangeInfo();
                changeInfo.setValue(status);
                changeInfo.setRedisKey(redisKey);
                changeInfo.setMapKey(mapKey);
                changeInfo.setCollectTime(new Date());
                req.getMaps().put(mapKey, changeInfo);

                String eventRedisKey = StatusInfoChangeTypeEnum.event_process_status.getCode();
                String eventMapKey = info.getAssetIp() + ":" + info.getAssetId() + ":" + info.getProcessName();
                AlarmTempReq alarmTempReq = new AlarmTempReq();
                if (status) {
                    alarmTempReq.setOrgMsg("恢复的进程ID:" + info.getProcessId() + " " + String.format(StatusInfoChangeTypeEnum.event_process_status.getDescr(), info.getProcessName(),info.getAlias(), info.getProcessId()));
                } else {
                    alarmTempReq.setOrgMsg(String.format(StatusInfoChangeTypeEnum.event_process_status.getDescr(), info.getProcessName(), info.getAlias(), info.getProcessId()));
                }
                alarmTempReq.setCollectValue(changeInfo.getValue().toString());
                alarmTempReq.setFlag(info.getProcessId());
                this.addEventStatus(StatusInfoChangeTypeEnum.event_process_status.getCode(),StatusInfoChangeTypeEnum.STATUS.getCode(), info.getProcessName(), status, req, changeInfo);
                IEvent event = eventInfoChangeManagerService.creatChangeEvent(info.getAssetId(), changeInfo, eventRedisKey, eventMapKey, status?EventLevelEnum.NORMAL.getCode():EventLevelEnum.ABNORMAL.getCode(),alarmTempReq);
                if (event != null) {
                    //被事件信息截取
                    changeInfo.setIsEvent(true);
                    //进程恢复
                    if (status) {
                        event.setRecoveryProcessIdDescr("【双击单活模式】恢复的进程ID:" + info.getProcessId() + " ");
                    }
                    event.setDescStr("【双击单活模式】"+String.format(StatusInfoChangeTypeEnum.event_process_status.getDescr(), info.getProcessName(),info.getAlias(), info.getProcessId()));
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
