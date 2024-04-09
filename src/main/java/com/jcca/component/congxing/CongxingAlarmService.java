package com.jcca.component.congxing;

import cn.hutool.core.date.DateUtil;
import com.jcca.common.enums.BusinessTypeEnums;
import com.jcca.component.casco.BrokerAlarmAdapter;
import com.jcca.component.dto.ItsmQueueReq;
import com.jcca.component.enums.ReceiveAlarmTypeEnum;
import com.jcca.component.event.EventLogicService;
import com.jcca.component.event.bean.CreateEventReq;
import com.jcca.component.event.constant.EventUniqueCode;
import com.jcca.web.event.enums.EventLevelEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;

/**
 * @author HanHW
 * @description 从兴通信质量监督告警处理
 * @className CongxingAlarmService
 * @date 2023/7/13 9:57
 * @since 2.0.5.0
 */
//@Service
public class CongxingAlarmService implements BrokerAlarmAdapter {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Resource
    private EventLogicService eventLogicService;

    @Override
    public String getCode() {
        return ReceiveAlarmTypeEnum.CONGXING.getCode();
    }

    @Override
    public void handle(ItsmQueueReq alarmDto) {
        CreateEventReq eventReq = new CreateEventReq();
        eventReq.setAssetId(alarmDto.getAssetId());
        eventReq.setUniqueCode(EventUniqueCode.CONGXING_ALARM);

        eventReq.setEventLevel(EventLevelEnum.ABNORMAL.getCode());
        int alarmState = alarmDto.getAlarmState();
        if (alarmState == 1) {
            eventReq.setEventLevel(EventLevelEnum.NORMAL.getCode());
        }
        eventReq.setOriginalMsg(alarmDto.getCollectValue());
        eventReq.setCollectValue(alarmDto.getAlarmContent());
        eventReq.setCreateTime(DateUtil.parseDateTime(alarmDto.getOccurTime()));
        eventReq.setGroupFlag(EventUniqueCode.CONGXING_ALARM);
        eventReq.setFlag(alarmDto.getIdStr());
        eventReq.setBusinessType(BusinessTypeEnums.CONGXING.code);

        try {
            eventLogicService.addEvent(eventReq);
        } catch (Exception e) {
            logger.error("保存从兴通信质量监督报警信息事件异常", e);
        }

    }
}
