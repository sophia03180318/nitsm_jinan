package com.jcca.component.tieke;

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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author HanHW
 * @description 铁科安全连接告警处理
 * @className TiekeSecureLinkService
 * @date 2023/6/13 10:12
 * @since 2.0.4.0
 */
@Slf4j
@Service
public class TiekeSecureLinkService implements BrokerAlarmAdapter {

    @Resource
    private EventLogicService eventLogicService;

    @Override
    public String getCode() {
        return ReceiveAlarmTypeEnum.TIEKE_SECURE_LINK.getCode();
    }

    @Override
    public void handle(ItsmQueueReq alarmDto) {
        CreateEventReq eventReq = new CreateEventReq();
        eventReq.setUniqueCode(EventUniqueCode.TIEKE_SECURE_LINK);
        eventReq.setAssetId(alarmDto.getAssetId());
        eventReq.setEventLevel(EventLevelEnum.ABNORMAL.getCode());
        eventReq.setAssetId(alarmDto.getAssetId());

        String linkStatus = alarmDto.getLinkStatus();
        String info = "铁科安全连接状态变化:[" + alarmDto.getProcessName()
                + "]连接状态变为:" + LinkStatusEnum.getName(linkStatus);
        if (linkStatus.equals(LinkStatusEnum.UP.name())) {
            eventReq.setEventLevel(EventLevelEnum.NORMAL.getCode());
        }
        eventReq.setOriginalMsg(alarmDto.getCollectValue());
        eventReq.setCreateTime(DateUtil.parseDateTime(alarmDto.getOccurTime()));
        eventReq.setGroupFlag(EventUniqueCode.TIEKE_SECURE_LINK);
        eventReq.setRepoMsg(info);
        eventReq.setFlag(alarmDto.getIdStr());
        eventReq.setBusinessType(BusinessTypeEnums.TIEKE.code);

        try {
            eventLogicService.addEvent(eventReq);
        } catch (Exception e) {
            log.error("保存铁科安全连接报警信息事件异常", e);
        }
    }
}
