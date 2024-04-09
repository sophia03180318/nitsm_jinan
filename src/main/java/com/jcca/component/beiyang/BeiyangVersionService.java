package com.jcca.component.beiyang;

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
 * @description 北羊软件版本变化告警处理
 * @className BeiyangVersionService
 * @date 2023/7/3 15:54
 * @since 2.0.5.0
 */
//@Service
public class BeiyangVersionService implements BrokerAlarmAdapter {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Resource
    private EventLogicService eventLogicService;

    @Override
    public String getCode() {
        return ReceiveAlarmTypeEnum.BEIYANG_SOFT_VERSION.getCode();
    }

    @Override
    public void handle(ItsmQueueReq alarmDto) {
        CreateEventReq eventReq = new CreateEventReq();
        eventReq.setUniqueCode(EventUniqueCode.BEIYANG_SOFT_VERSION);
        eventReq.setAssetId(alarmDto.getAssetId());
        eventReq.setEventLevel(EventLevelEnum.NOTIFY.getCode());
        eventReq.setOriginalMsg(alarmDto.getCollectValue());
        eventReq.setCreateTime(DateUtil.parseDateTime(alarmDto.getOccurTime()));
        eventReq.setGroupFlag(EventUniqueCode.BEIYANG_SOFT_VERSION);
        eventReq.setBaseValue(alarmDto.getCascoSoftName());
        String alarmCode = alarmDto.getEntityId() + "-" + alarmDto.getAbFlag() + "-" + alarmDto.getNowVersion();
        eventReq.setFlag(alarmCode);
        eventReq.setBusinessType(BusinessTypeEnums.BEIYANG.code);

        try {
            eventLogicService.addEvent(eventReq);
        } catch (Exception e) {
            logger.error("保存北羊软件版本变化报警信息事件异常", e);
        }
    }
}
