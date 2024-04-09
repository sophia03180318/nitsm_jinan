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
 * @description 铁科软件版本变化处理
 * @className TiekeVersionService
 * @date 2023/6/13 10:09
 * @since 2.0.4.0
 */
@Slf4j
@Service
public class TiekeVersionService implements BrokerAlarmAdapter {

    @Resource
    private EventLogicService eventLogicService;

    @Override
    public String getCode() {
        return ReceiveAlarmTypeEnum.TIEKE_SOFT_VERSION.getCode();
    }

    @Override
    public void handle(ItsmQueueReq alarmDto) {
        CreateEventReq eventReq = new CreateEventReq();
        eventReq.setUniqueCode(EventUniqueCode.TIEKE_SOFT_VERSION);
        eventReq.setAssetId(alarmDto.getAssetId());
        eventReq.setEventLevel(EventLevelEnum.NOTIFY.getCode());
        eventReq.setOriginalMsg(alarmDto.getCollectValue());
        eventReq.setCreateTime(DateUtil.parseDateTime(alarmDto.getOccurTime()));
        eventReq.setGroupFlag(EventUniqueCode.TIEKE_SOFT_VERSION);
        String alarmCode = alarmDto.getEntityId() + "-" + alarmDto.getAbFlag() + "-" + alarmDto.getNowVersion();
        eventReq.setFlag(alarmCode);
        eventReq.setBusinessType(BusinessTypeEnums.TIEKE.code);

        try {
            eventLogicService.addEvent(eventReq);
        } catch (Exception e) {
            log.error("保存铁科软件版本变化报警信息事件异常", e);
        }
    }
}
