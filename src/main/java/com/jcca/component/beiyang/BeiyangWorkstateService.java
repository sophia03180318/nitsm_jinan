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
 * @description 北羊工作状态变化告警处理
 * @className BeiyangWorkstateService
 * @date 2023/7/3 15:55
 * @since 2.0.5.0
 */
//@Service
public class BeiyangWorkstateService implements BrokerAlarmAdapter {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Resource
    private EventLogicService eventLogicService;

    @Override
    public String getCode() {
        return ReceiveAlarmTypeEnum.BEIYANG_WORKSTATE.getCode();
    }

    @Override
    public void handle(ItsmQueueReq alarmDto) {
        int alarmState = alarmDto.getAlarmState();

        CreateEventReq eventReq = new CreateEventReq();
        eventReq.setUniqueCode(EventUniqueCode.BEIYANG_WORKSTATE);
        eventReq.setAssetId(alarmDto.getAssetId());

        if (alarmState == EventLevelEnum.NORMAL.getCode()) {
            eventReq.setEventLevel(EventLevelEnum.NORMAL.getCode());
        } else {
            eventReq.setEventLevel(EventLevelEnum.ABNORMAL.getCode());
        }
        eventReq.setOriginalMsg(alarmDto.getCollectValue());
        eventReq.setCreateTime(DateUtil.parseDateTime(alarmDto.getOccurTime()));
        eventReq.setGroupFlag(EventUniqueCode.BEIYANG_WORKSTATE);
        eventReq.setFlag(alarmDto.getIdStr());
        eventReq.setBusinessType(BusinessTypeEnums.BEIYANG.code);

        try {
            eventLogicService.addEvent(eventReq);
        } catch (Exception e) {
            logger.error("保存北羊设备软件工作状态变化事件异常", e);
        }
    }
}
