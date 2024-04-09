package com.jcca.component.tieke;

import cn.hutool.core.date.DateUtil;
import com.jcca.common.enums.BusinessTypeEnums;
import com.jcca.component.casco.BrokerAlarmAdapter;
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
 * @author HanHW
 * @description 铁科工作状态告警处理
 * @className TiekeWorkstateService
 * @date 2023/6/13 9:57
 * @since 2.0.4.0
 */
@Service
@Slf4j
public class TiekeWorkstateService implements BrokerAlarmAdapter {

    @Resource
    private EventLogicService eventLogicService;

    @Override
    public String getCode() {
        return ReceiveAlarmTypeEnum.TIEKE_WORK_STATE.getCode();
    }

    @Override
    public void handle(ItsmQueueReq alarmDto) {
        int alarmState = alarmDto.getAlarmState();

        CreateEventReq eventReq = new CreateEventReq();
        eventReq.setUniqueCode(EventUniqueCode.TIEKE_WORK_STATE);
        eventReq.setAssetId(alarmDto.getAssetId());

        if (alarmState == EventLevelEnum.NORMAL.getCode()) {
            eventReq.setEventLevel(EventLevelEnum.NORMAL.getCode());
        } else {
            eventReq.setEventLevel(EventLevelEnum.ABNORMAL.getCode());
        }
        eventReq.setOriginalMsg(alarmDto.getCollectValue());
        eventReq.setCreateTime(DateUtil.parseDateTime(alarmDto.getOccurTime()));
        eventReq.setGroupFlag(EventUniqueCode.TIEKE_WORK_STATE);
        eventReq.setFlag(alarmDto.getIdStr());
        eventReq.setBusinessType(BusinessTypeEnums.TIEKE.code);

        try {
            eventLogicService.addEvent(eventReq);
        } catch (Exception e) {
            log.error("保存铁科设备软件工作状态变化事件异常", e);
        }

    }
}
