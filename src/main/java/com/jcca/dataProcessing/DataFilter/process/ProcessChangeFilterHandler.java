package com.jcca.dataProcessing.DataFilter.process;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.redis.service.RedisService;
import com.jcca.common.utils.AppLogUtils;
import com.jcca.component.constants.RedisQueueConst;
import com.jcca.component.dto.ReceiveAlarmDto;
import com.jcca.component.process.bean.ProcessAlarmQueueBean;
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
import java.util.ArrayList;
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
    @Resource
    private RedisService redisService;

    private boolean flag = false;

    //    @Scheduled(cron = "0 0/2 * * * *")
    public void processChange() {

        List<ProcessAlarmQueueBean> queueList = new ArrayList<>();

        ProcessAlarmQueueBean bean1 = new ProcessAlarmQueueBean();
        bean1.setProcessName("Notepad.exe");
        bean1.setHostMode(1);
        bean1.setProcessId(flag ? "12345" : "0");
        bean1.setAssetIp("192.168.1.188");
        bean1.setProcessStatus(flag);

        ProcessAlarmQueueBean bean2 = new ProcessAlarmQueueBean();
        bean2.setProcessName("Notepad.exe");
        bean2.setHostMode(1);
        bean2.setProcessId(flag ? "0" : "54321");
        bean2.setAssetIp("192.168.20.130");
        bean2.setProcessStatus(!flag);

        queueList.add(bean1);
        queueList.add(bean2);

        ReceiveAlarmDto dto1 = new ReceiveAlarmDto();
        dto1.setOccurTime(System.currentTimeMillis() + "");
        dto1.setCategory("27");
        dto1.setAssetIp("once");
        dto1.setProcessChange("once"); // 因json处理时不能有相同名称字段，因此增加此字段
        dto1.setContent(JSONUtil.parseArray(queueList).toString());
        String queueMsg1 = JSONUtil.toJsonStr(dto1);
        redisService.convertAndSend(RedisQueueConst.ALARM_QUEUE, queueMsg1);
        flag = !flag;
    }

    @Override
    public boolean handler(ProcessGroupEntity entity) throws Exception {
        List<ProcessAlarmQueueEntity> queueObj = entity.getQueueObj();
        for (ProcessAlarmQueueEntity info : queueObj) {
            String processChange = info.getProcessChange();
            if (StringUtils.isEmpty(processChange)) {
                continue;
            }
            String processId = info.getProcessId();
            if (!"0".equals(processId)) {
                continue;
            }

            AppLogUtils.buildLogInfo(LogFunctionEnum.DATA_PROCESS_CHANGE, info.getProcessName() + "进程切换告警处理", info);

            String redisKey = info.getAssetIp() + ":" + info.getAssetId() + ":" + StatusInfoChangeTypeEnum.status_process_status.getCode();
            String mapKey = info.getProcessName() + "_processChange";
            Boolean processStatus = info.getProcessStatus();
            Boolean flag = eventInfoChangeManagerService.infoIschangeFirst(redisKey, mapKey, processStatus);
            if (flag == null || flag) {
                ChangeInfo changeInfo = new ChangeInfo();
                changeInfo.setValue(processStatus);
                changeInfo.setRedisKey(redisKey);
                changeInfo.setMapKey(mapKey);
                changeInfo.setCollectTime(new Date());
                entity.getMaps().put(mapKey, changeInfo);

                Asset asset = this.getGroupAsset(info.getAssetId());

                AlarmTempReq alarmTempReq = new AlarmTempReq();
                alarmTempReq.setOrgMsg(String.format(StatusInfoChangeTypeEnum.event_process_once.getDescr(),
                        info.getProcessName(), asset.getName() + "(" + asset.getIp() + ")"));
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
                            info.getProcessName(), asset.getName() + "(" + asset.getIp() + ")"));
                    this.dispatureEvent(event);
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
