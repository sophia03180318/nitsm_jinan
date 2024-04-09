package com.jcca.component.beiyang;

import cn.hutool.core.date.DateUtil;
import com.jcca.common.enums.BusinessTypeEnums;
import com.jcca.component.casco.BrokerAlarmAdapter;
import com.jcca.component.crsc.enums.LinkStatusEnum;
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
 * @description 北羊通道连接告警处理
 * @className BeiyangChannelService
 * @date 2023/7/3 15:56
 * @since 2.0.5.0
 */
//@Service
public class BeiyangChannelService implements BrokerAlarmAdapter {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Resource
    private EventLogicService eventLogicService;

    @Override
    public String getCode() {
        return ReceiveAlarmTypeEnum.BEIYANG_CHANNEL_LINK.getCode();
    }

    @Override
    public void handle(ItsmQueueReq alarmDto) {
        CreateEventReq eventReq = new CreateEventReq();
        eventReq.setUniqueCode(EventUniqueCode.BEIYANG_CHANNEL_LINK);
        eventReq.setAssetId(alarmDto.getAssetId());
        eventReq.setEventLevel(EventLevelEnum.ABNORMAL.getCode());
        eventReq.setAssetId(alarmDto.getAssetId());

        String linkStatus = alarmDto.getLinkStatus();
        if (linkStatus.equals(LinkStatusEnum.UP.name())) {
            eventReq.setEventLevel(EventLevelEnum.NORMAL.getCode());
        }
        eventReq.setOriginalMsg(alarmDto.getCollectValue());
        eventReq.setCreateTime(DateUtil.parseDateTime(alarmDto.getOccurTime()));
        eventReq.setGroupFlag(EventUniqueCode.BEIYANG_CHANNEL_LINK);
        eventReq.setFlag(alarmDto.getIdStr());
        eventReq.setBusinessType(BusinessTypeEnums.BEIYANG.code);

        try {
            eventLogicService.addEvent(eventReq);
        } catch (Exception e) {
            logger.error("保存北羊通道连接报警信息事件异常", e);
        }
    }
}
