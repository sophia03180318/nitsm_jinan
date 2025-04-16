package com.jcca.dataProcessing.DataFilter.process;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
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
        for (ProcessAlarmQueueEntity info : queueObj) {
            String processChange = info.getProcessChange();
            if (StringUtils.isEmpty(processChange)) {
                continue;
            }

            AppLogUtils.buildLogInfo(LogFunctionEnum.DATA_PROCESS_CHANGE, info.getProcessName() + "进程切换告警处理", info);

            String redisKey = info.getAssetIp() + ":" + info.getAssetId() + ":" + StatusInfoChangeTypeEnum.status_process_status.getCode();
            String mapKey = info.getProcessName() + "_processChange";
            Boolean processStatus = info.getProcessStatus();
            Boolean flag = eventInfoChangeManagerService.infoIschangeFirst(redisKey, mapKey, processStatus);
            if (processStatus) {
                ChangeInfo changeInfo = new ChangeInfo();
                changeInfo.setValue(processStatus);
                changeInfo.setRedisKey(redisKey);
                changeInfo.setMapKey(mapKey);
                changeInfo.setCollectTime(new Date());
                entity.getMaps().put(mapKey, changeInfo);

                Asset asset = this.getGroupAsset(info.getAssetId());

                AlarmTempReq alarmTempReq = new AlarmTempReq();
                alarmTempReq.setOrgMsg(String.format(StatusInfoChangeTypeEnum.event_process_once.getDescr(),
                        info.getProcessName(), info.getAlias(), asset.getName() + "(" + asset.getIp() + ")"));
                alarmTempReq.setCollectValue(changeInfo.getValue().toString());
                alarmTempReq.setFlag(info.getProcessId());
                this.addEventStatus(StatusInfoChangeTypeEnum.event_process_once.getCode(), StatusInfoChangeTypeEnum.STATUS.getCode(),
                        info.getProcessName(), EventLevelEnum.ABNORMAL.getCode(), entity, changeInfo);

                String eventRedisKey = StatusInfoChangeTypeEnum.event_process_once.getCode();
                String eventMapKey = info.getAssetIp() + ":" + info.getAssetId() + ":" + info.getProcessName();
                IEvent event = eventInfoChangeManagerService.creatChangeEvent(info.getAssetId(), changeInfo, eventRedisKey,
                        eventMapKey, EventLevelEnum.ABNORMAL.getCode(), alarmTempReq);
                if (event != null) {
                    changeInfo.setIsEvent(true);
                    event.setDescStr(String.format(StatusInfoChangeTypeEnum.event_process_once.getDescr(),
                            info.getProcessName(), info.getAlias(), asset.getName() + "(" + asset.getIp() + ")"));
                    this.dispatureEvent(event);
                    return false;
                }
            }
        }
        return true;
    }

    private Asset getGroupAsset(String assetId) {
        Asset asset = assetService.getById(assetId);
        QueryWrapper<Asset> query = Wrappers.query();
        query.eq("ASSET_CODE", asset.getAssetCode());
        query.ne("ID", asset.getId());
        List<Asset> list = assetService.list(query);
        return list.get(0);
    }

    @Override
    public boolean isNeedNexthandle(Boolean flag) {
        return flag;
    }
}
