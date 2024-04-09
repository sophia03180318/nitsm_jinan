package com.jcca.component.crsc;

import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONUtil;
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

import javax.annotation.Resource;

/**
 * @Description 通号连接状态改变处理
 * @ClassName CrscLinkChangeService
 * @Date 2022/5/11 10:55
 * @Author hanwone
 * @Since 2.0.0.1
 */
//@Service
@Slf4j
@Deprecated
public class CrscLinkChangeService implements BrokerAlarmAdapter {

    @Resource
    private EventLogicService eventLogicService;

    @Override
    public String getCode() {
        return ReceiveAlarmTypeEnum.CRSC_LINK_CHANGE.getCode();
    }

    @Override
    public void handle(ItsmQueueReq alarmDto) {
        log.info("处理通号进程连接状态改变告警数据：" + JSONUtil.toJsonStr(alarmDto));

        CreateEventReq eventReq = new CreateEventReq();
        eventReq.setUniqueCode(EventUniqueCode.CRSC_LINK);
        eventReq.setAssetId(alarmDto.getAssetId());
        eventReq.setEventLevel(EventLevelEnum.ABNORMAL.getCode());
        eventReq.setAssetId(alarmDto.getAssetId());

        String linkStatus = alarmDto.getLinkStatus();
        String info = "通号进程连接变化:[" + alarmDto.getProcessName()
                + "]连接状态变为:" + LinkStatusEnum.getName(linkStatus);
        if (linkStatus.equals(LinkStatusEnum.UP.name())) {
            eventReq.setEventLevel(EventLevelEnum.NORMAL.getCode());
        }
        eventReq.setOriginalMsg(alarmDto.getIdStr());
        eventReq.setCreateTime(DateUtil.parseDateTime(alarmDto.getOccurTime()));
        eventReq.setGroupFlag(EventUniqueCode.CRSC_LINK);
        eventReq.setRepoMsg(info);
        eventReq.setBusinessType(BusinessTypeEnums.TONGHAO.code);

        try {
            eventLogicService.addEvent(eventReq);
        } catch (Exception e) {
            log.error("保存通号报警信息事件异常", e);
        }
    }
}
