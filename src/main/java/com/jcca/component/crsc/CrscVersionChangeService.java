package com.jcca.component.crsc;

import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONUtil;
import com.jcca.common.enums.BusinessTypeEnums;
import com.jcca.component.casco.BrokerAlarmAdapter;
import com.jcca.component.dto.ItsmQueueReq;
import com.jcca.component.enums.ReceiveAlarmTypeEnum;
import com.jcca.component.event.EventLogicService;
import com.jcca.component.event.bean.CreateEventReq;
import com.jcca.component.event.constant.EventUniqueCode;
import com.jcca.web.event.enums.EventLevelEnum;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;

/**
 * @Description 通号软件版本变化处理
 * @ClassName CrscVersionChangeService
 * @Date 2022/5/11 10:56
 * @Author hanwone
 * @Since 2.0.0.1
 */
//@Service
@Slf4j
@Deprecated
public class CrscVersionChangeService implements BrokerAlarmAdapter {
    @Resource
    private EventLogicService eventLogicService;

    @Override
    public String getCode() {
        return ReceiveAlarmTypeEnum.CRSC_VERSION_CHANGE.getCode();
    }

    @Override
    public void handle(ItsmQueueReq alarmDto) {
        log.info("处理通号软件版本变化数据：" + JSONUtil.toJsonStr(alarmDto));

        CreateEventReq eventReq = new CreateEventReq();
        eventReq.setUniqueCode(EventUniqueCode.CRSC_VERSION);
        eventReq.setAssetId(alarmDto.getAssetId());
        eventReq.setEventLevel(EventLevelEnum.NOTIFY.getCode());

        String info = "通号软件版本变化：设备ID为[" + alarmDto.getEntityId() + "]的设备软件版本由["
                + alarmDto.getOldVersion() + "]变更为[" + alarmDto.getNowVersion() + "]";
        eventReq.setOriginalMsg(info);
        eventReq.setCreateTime(DateUtil.parseDateTime(alarmDto.getOccurTime()));
        eventReq.setGroupFlag(EventUniqueCode.CRSC_VERSION);
        String alarmCode = alarmDto.getEntityId() + "-" + alarmDto.getAbFlag() + "-" + alarmDto.getNowVersion();
        eventReq.setFlag(alarmCode);
        eventReq.setBusinessType(BusinessTypeEnums.TONGHAO.code);

        try {
            eventLogicService.addEvent(eventReq);
        } catch (Exception e) {
            log.error("保存通号报警信息事件异常", e);
        }
    }
}
