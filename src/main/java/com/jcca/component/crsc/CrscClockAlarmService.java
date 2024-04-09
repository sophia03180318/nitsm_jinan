package com.jcca.component.crsc;

import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONUtil;
import com.jcca.common.enums.BusinessTypeEnums;
import com.jcca.component.casco.BrokerAlarmAdapter;
import com.jcca.component.crsc.constant.BusinessConst;
import com.jcca.component.dto.ItsmQueueReq;
import com.jcca.component.enums.ReceiveAlarmTypeEnum;
import com.jcca.component.event.EventLogicService;
import com.jcca.component.event.bean.CreateEventReq;
import com.jcca.component.event.constant.EventUniqueCode;
import com.jcca.web.event.enums.EventLevelEnum;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;

/**
 * @Description 通号时钟告警
 * @ClassName CrscClockAlarmService
 * @Date 2022/5/9 14:08
 * @Author hanwone
 * @Since 2.0.0.1
 */
//@Service
@Slf4j
@Deprecated
public class CrscClockAlarmService implements BrokerAlarmAdapter {

    @Resource
    private EventLogicService eventLogicService;

    @Override
    public String getCode() {
        return ReceiveAlarmTypeEnum.CRSC_CLOCK_ALARM.getCode();
    }

    @Override
    public void handle(ItsmQueueReq alarmDto) {
        log.info("处理通号时钟告警数据：" + JSONUtil.toJsonStr(alarmDto));
        int alarmState = alarmDto.getAlarmState();

        CreateEventReq eventReq = new CreateEventReq();
        eventReq.setAssetId(alarmDto.getAssetId());
        eventReq.setEventLevel(EventLevelEnum.ABNORMAL.getCode());
        eventReq.setUniqueCode(EventUniqueCode.CRSC_CLOCK_ALARM);

        String info = "通号设备发生时钟告警,报警内容为:" + alarmDto.getAlarmContent();
        if (alarmState == BusinessConst.CRSC_ALARM_RECOVER) {
            eventReq.setEventLevel(EventLevelEnum.NORMAL.getCode());
            info = "通号设备时钟告警恢复,报警内容为:" + alarmDto.getAlarmContent();
        }
        eventReq.setOriginalMsg(info);
        eventReq.setCreateTime(DateUtil.parseDateTime(alarmDto.getOccurTime()));
        eventReq.setGroupFlag(EventUniqueCode.CRSC_CLOCK_ALARM);
        eventReq.setBusinessType(BusinessTypeEnums.TONGHAO.code);

        try {
            eventLogicService.addEvent(eventReq);
        } catch (Exception e) {
            log.error("保存通号报警信息事件异常", e);
        }
    }
}
