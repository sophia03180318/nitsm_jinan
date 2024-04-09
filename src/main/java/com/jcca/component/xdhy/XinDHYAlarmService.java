package com.jcca.component.xdhy;

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
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author HanHW
 * @description 处理信达环宇syslog日志告警
 * @className XinDHYAlarmService
 * @date 2023/6/26 14:48
 * @since 2.0.4.0
 */
@Service
public class XinDHYAlarmService implements BrokerAlarmAdapter {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Resource
    private EventLogicService eventLogicService;

    @Override
    public String getCode() {
        return ReceiveAlarmTypeEnum.XDHY_LOG.getCode();
    }

    @Override
    public void handle(ItsmQueueReq alarmDto) {
        int alarmState = alarmDto.getAlarmState();

        CreateEventReq eventReq = new CreateEventReq();
        eventReq.setUniqueCode(EventUniqueCode.XDHY_SYSLOG);
        eventReq.setAssetId(alarmDto.getAssetId());

        if (alarmState == EventLevelEnum.NORMAL.getCode()) {
            eventReq.setEventLevel(EventLevelEnum.NORMAL.getCode());
        } else {
            eventReq.setEventLevel(EventLevelEnum.ABNORMAL.getCode());
        }
        eventReq.setOriginalMsg(alarmDto.getAlarmContent());
        eventReq.setCollectValue(alarmDto.getCollectValue());
        eventReq.setCreateTime(DateUtil.parseDateTime(alarmDto.getOccurTime()));
        eventReq.setGroupFlag(EventUniqueCode.XDHY_SYSLOG);
        eventReq.setFlag(alarmDto.getIdStr());
        eventReq.setBusinessType(BusinessTypeEnums.XDHY.code);

        try {
            eventLogicService.addEvent(eventReq);
        } catch (Exception e) {
            logger.error("保存信达环宇syslog告警信息异常", e);
        }
    }
}
