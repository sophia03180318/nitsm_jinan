package com.jcca.dataProcessing.DataFilter.process;

import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.utils.AppLogUtils;
import com.jcca.dataProcessing.Entity.ChangeInfo;
import com.jcca.dataProcessing.Entity.CollectProcessEntity;
import com.jcca.dataProcessing.enums.StatusInfoChangeTypeEnum;
import com.jcca.dataProcessing.manager.IEventInfoManagerService;
import com.jcca.dataProcessing.manager.bean.AlarmTempReq;
import com.jcca.dataProcessing.support.IEvent;
import com.jcca.dataProcessing.support.IFilterHandler;
import com.jcca.web.event.enums.EventLevelEnum;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;

/**
 * @author Zhaozheng
 * @description TODO 进程普通状态 信息过滤处理类 只有车站设备需要走此处
 * @className ProcessStateFilterHandler
 * @date 2023/10/27 9:57
 * @since 2.1.0.0
 */
@Component("processStateFilterHandler")
public class ProcessStateFilterHandler extends IFilterHandler<CollectProcessEntity> {

    @Resource
    private IEventInfoManagerService eventInfoChangeManagerService;

    @Override
    public boolean handler(CollectProcessEntity entity) {
        AppLogUtils.buildLogInfo(LogFunctionEnum.DATA_PROCESS_SINGLE, "普通模式进程状态处理", entity.getAssetIp());
        String redisKey = entity.getAssetIp() + ":" + entity.getAssetId() + ":" + StatusInfoChangeTypeEnum.status_process.getCode() + ":" + entity.getName();
        if (!entity.getStationAsset()) {
            return true;
        }
        //只走车站的进程状态判断
        String mapKey = StatusInfoChangeTypeEnum.status_process_status.getCode();
        boolean flag = eventInfoChangeManagerService.infoIschange(entity.getInspectRecordId(),redisKey, mapKey, entity.getStatus());
        if (flag) {
            ChangeInfo changeInfo = new ChangeInfo();
            changeInfo.setValue(entity.getStatus());
            changeInfo.setRedisKey(redisKey);
            changeInfo.setMapKey(mapKey);
            changeInfo.setCollectTime(new Date());
            entity.getMaps().put(mapKey, changeInfo);

            String eventRedisKey = StatusInfoChangeTypeEnum.event_process_status.getCode();
            String eventMapKey = entity.getAssetIp() + "_" + entity.getAssetId() + "_" + entity.getName();

            Integer status = entity.getStatus() ? EventLevelEnum.NORMAL.getCode() : EventLevelEnum.ABNORMAL.getCode();
            AlarmTempReq alarmTempReq = new AlarmTempReq();
            if (status == EventLevelEnum.NORMAL.getCode()) {
                alarmTempReq.setOrgMsg(" 恢复的进程ID:" + entity.getProcessId() + " "
                        + String.format(StatusInfoChangeTypeEnum.event_process_status.getDescr(), entity.getName(), entity.getAlias(), entity.getProcessId()));
            } else {
                alarmTempReq.setOrgMsg(String.format(StatusInfoChangeTypeEnum.event_process_status.getDescr(), entity.getName(), entity.getAlias(), entity.getProcessId()));
            }
            alarmTempReq.setCollectValue(changeInfo.getValue().toString());
            alarmTempReq.setFlag(entity.getProcessId());
            this.addEventStatus(StatusInfoChangeTypeEnum.event_process_status.getCode(), StatusInfoChangeTypeEnum.STATUS.getCode(), entity.getName(), status, entity, changeInfo);
            IEvent event = eventInfoChangeManagerService.creatChangeEvent(entity.getAssetId(), changeInfo, eventRedisKey, eventMapKey, status, alarmTempReq,entity.getInspectRecordId());
            if (event != null) {
                //被事件信息截取
                changeInfo.setIsEvent(true);
                //进程恢复
                if (status == EventLevelEnum.NORMAL.getCode()) {
                    event.setRecoveryProcessIdDescr(" 恢复的进程ID:" + entity.getProcessId() + " ");
                }
                event.setDescStr(String.format(StatusInfoChangeTypeEnum.event_process_status.getDescr(), entity.getName(), entity.getAlias(), entity.getProcessId()));
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
