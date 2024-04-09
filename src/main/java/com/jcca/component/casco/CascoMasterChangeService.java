package com.jcca.component.casco;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.jcca.common.enums.BusinessTypeEnums;
import com.jcca.common.utils.MyIdUtil;
import com.jcca.component.casco.enums.HostRunStatusEnum;
import com.jcca.component.dto.ItsmQueueReq;
import com.jcca.component.enums.ReceiveAlarmTypeEnum;
import com.jcca.component.event.EventLogicService;
import com.jcca.component.event.bean.CreateEventReq;
import com.jcca.component.event.constant.EventGroupConstant;
import com.jcca.component.event.constant.EventUniqueCode;
import com.jcca.web.event.enums.EventLevelEnum;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import java.util.Date;

/**
 * @ClassName CascoMasterChangeService
 * @Description 处理卡斯科主备切换告警
 * @Author oamwh
 * @Date 2021/1/7 9:49
 * @Version ITSM2.0
 **/
//@Service
@Slf4j
public class CascoMasterChangeService implements BrokerAlarmAdapter {

    @Resource
    private EventLogicService eventLogicServ;

    @Override
    public String getCode() {
        return ReceiveAlarmTypeEnum.CASCO_MASTER_CHANGE.getCode();
    }

    @Override
    public void handle(ItsmQueueReq alarmData) {
        String assetId = alarmData.getAssetId();
        log.info("开始处理卡斯柯主备告警,收到的告警信息为:{}", alarmData.toString());
        synchronized (assetId.intern()) {
            // 告警code
            String alarmCode = assetId + ReceiveAlarmTypeEnum.CASCO_MASTER_CHANGE.name();
            CreateEventReq eventReq = new CreateEventReq();

            String occurTime = alarmData.getOccurTime();
            if (StrUtil.isNotEmpty(occurTime)) {
                eventReq.setCreateTime(DateUtil.parse(occurTime, "yyyy-MM-dd HH:mm:ss"));
            } else {
                eventReq.setCreateTime(new Date());
            }

            // 上来的信息为主/备
            if (alarmData.getHostType().equals(HostRunStatusEnum.MASTER.name())
                    || alarmData.getHostType().equals(HostRunStatusEnum.BACK.name())) {

                // 上来的信息为主到备直接上信息通知,反之亦然
                if (alarmData.getHostType().equals(HostRunStatusEnum.MASTER.name())
                        || alarmData.getHostType().equals(HostRunStatusEnum.BACK.name())) {

                    if (alarmData.getOldHostType().equals(HostRunStatusEnum.MASTER.name())
                            || alarmData.getOldHostType().equals(HostRunStatusEnum.BACK.name())) {

                        // 事件性质
                        eventReq.setEventLevel(EventLevelEnum.NOTIFY.getCode());
                        String format = String.format("业务告警-卡斯柯主备变化：实体号为:%s，的设备切换到%s",
                                alarmData.getEntityId(),
                                HostRunStatusEnum.getName(alarmData.getHostType()));
                        eventReq.setOriginalMsg(format);
                        eventReq.setCollectValue(HostRunStatusEnum.getName(alarmData.getHostType()));
                        eventReq.setAssetId(assetId);
                        eventReq.setUniqueCode(EventUniqueCode.CASCO_CHANGE);
                        // 使用说明见 AlarmEvent.flag
                        eventReq.setFlag(MyIdUtil.getId());
                        eventReq.setGroupFlag(EventGroupConstant.CACSON_MASTER_CHANGE);

                        try {
                            eventLogicServ.addEvent(eventReq);
                        } catch (Exception e) {
                            log.error("处理casco连接事件异常：{}", e.getMessage(), e);
                        }

                        return;

                    }
                }

                // 事件性质
                eventReq.setEventLevel(EventLevelEnum.NORMAL.getCode());
                String format = String.format("业务告警-卡斯柯主备变化：实体号为:%s，的设备连接中断后恢复", alarmData.getEntityId());
                eventReq.setOriginalMsg(format);
                eventReq.setCollectValue(HostRunStatusEnum.getName(alarmData.getHostType()));
                eventReq.setAssetId(assetId);
                eventReq.setUniqueCode(EventUniqueCode.CASCO_MASTER_STOP);
                // 使用说明见 AlarmEvent.flag
                eventReq.setFlag(EventUniqueCode.CASCO_MASTER_STOP + alarmData.getAttrGroupId() + alarmData.getAttrIndex());
                eventReq.setGroupFlag(EventGroupConstant.CACSON_MASTER_CHANGE);
                eventReq.setBusinessType(BusinessTypeEnums.CASCO.code);

                try {
                    eventLogicServ.addEvent(eventReq);
                } catch (Exception e) {
                    log.error("处理casco连接事件异常：{}", e.getMessage(), e);
                }
                return;
            }


            if (alarmData.getHostType().equals(HostRunStatusEnum.STOP.name())) {

                // 事件性质
                eventReq.setEventLevel(EventLevelEnum.ABNORMAL.getCode());
                String format = String.format("业务告警-卡斯柯主备变化：实体号为:%s，的设备连接中断", alarmData.getEntityId());
                eventReq.setOriginalMsg(format);
                eventReq.setCollectValue(HostRunStatusEnum.getName(alarmData.getHostType()));
                eventReq.setAssetId(assetId);
                eventReq.setUniqueCode(EventUniqueCode.CASCO_MASTER_STOP);
                // 使用说明见 AlarmEvent.flag
                eventReq.setFlag(EventUniqueCode.CASCO_MASTER_STOP + alarmData.getAttrGroupId() + alarmData.getAttrIndex());
                eventReq.setGroupFlag(EventUniqueCode.CASCO_MASTER_STOP);

                try {
                    eventLogicServ.addEvent(eventReq);
                } catch (Exception e) {
                    log.error("处理casco连接事件异常：{}", e.getMessage(), e);
                }

            }
        }

    }

}
