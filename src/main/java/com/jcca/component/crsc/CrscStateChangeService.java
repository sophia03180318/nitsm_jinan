package com.jcca.component.crsc;

import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONUtil;
import com.jcca.common.enums.BusinessTypeEnums;
import com.jcca.component.casco.BrokerAlarmAdapter;
import com.jcca.component.crsc.constant.BusinessConst;
import com.jcca.component.crsc.enums.ProcessStateEnum;
import com.jcca.component.dto.ItsmQueueReq;
import com.jcca.component.enums.ReceiveAlarmTypeEnum;
import com.jcca.component.event.EventLogicService;
import com.jcca.component.event.bean.CreateEventReq;
import com.jcca.component.event.constant.EventUniqueCode;
import com.jcca.web.event.enums.EventLevelEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @Description 通号进程状态变化告警
 * @ClassName CrscStateChangeService
 * @Date 2022/5/9 13:54
 * @Author hanwone
 * @Since 2.0.0.1
 */
@Slf4j
@Service
@Deprecated
public class CrscStateChangeService implements BrokerAlarmAdapter {

    @Resource
    private EventLogicService eventLogicService;

    @Override
    public String getCode() {
        return ReceiveAlarmTypeEnum.CRSC_PROCESS_STATE.getCode();
    }

    @Override
    public void handle(ItsmQueueReq alarmData) {
        log.info("处理通号进程状态告警数据：" + JSONUtil.toJsonStr(alarmData));
        int processState = alarmData.getProcessState();
        CreateEventReq eventReq = new CreateEventReq();
        eventReq.setUniqueCode(EventUniqueCode.CRSC_PROCESS_STATE);
        eventReq.setAssetId(alarmData.getAssetId());
        eventReq.setEventLevel(EventLevelEnum.NORMAL.getCode());
        if (processState != BusinessConst.CRSC_PROCESS_STATE_1) {
            eventReq.setEventLevel(EventLevelEnum.ABNORMAL.getCode());
        }

        String format = String.format("通号设备进程状态变化：进程名称为[%s]的进程状态变为[%s]",
                alarmData.getProcessName(), ProcessStateEnum.getName(alarmData.getProcessState()));
        eventReq.setOriginalMsg(format);
        eventReq.setCollectValue(ProcessStateEnum.getName(alarmData.getProcessState()));
        eventReq.setCreateTime(DateUtil.parseDateTime(alarmData.getOccurTime()));
        eventReq.setGroupFlag(EventUniqueCode.CRSC_PROCESS_STATE);
        eventReq.setBusinessType(BusinessTypeEnums.TONGHAO.code);

        try {
            eventLogicService.addEvent(eventReq);
        } catch (Exception e) {
            log.error("保存通号进程状态变化事件异常", e);
        }
    }
}
