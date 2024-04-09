package com.jcca.component.casco;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.jcca.common.enums.BusinessTypeEnums;
import com.jcca.component.casco.enums.LinkStatusEnum;
import com.jcca.component.dto.ItsmQueueReq;
import com.jcca.component.enums.ReceiveAlarmTypeEnum;
import com.jcca.component.event.EventLogicService;
import com.jcca.component.event.bean.CreateEventReq;
import com.jcca.component.event.constant.EventGroupConstant;
import com.jcca.component.event.constant.EventUniqueCode;
import com.jcca.web.event.enums.EventLevelEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;

/**
 * @ClassName CascoLinkAlarmService
 * @Description 处理卡斯科连接告警
 * @Author oamwh
 * @Date 2021/1/7 9:48
 * @Version ITSM2.0
 **/
//@Service
@Slf4j
public class CascoLinkAlarmService implements BrokerAlarmAdapter {

    @Resource
    private EventLogicService eventLogicServ;

    @Override
    public String getCode() {
        return ReceiveAlarmTypeEnum.CASCO_LINK.getCode();// 适配枚举ReceiveAlarmTypeEnum的code
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handle(ItsmQueueReq alarmData) {
        String assetId = alarmData.getAssetId();// ITSM资产ID
        if (StrUtil.isEmpty(assetId)) {
            log.info("收到卡斯科告警资产ID为空，收到数据为：{}", JSONUtil.toJsonStr(alarmData));
            return;
        }
        log.info("开始处理卡斯柯连接告警,收到的告警信息:{}", alarmData.toString());
        // 资产ID + entityID + AB标识 + 链接TYPE类型 + 索引号去确定告警code
        String alarmCode = assetId + alarmData.getEntityId() + alarmData.getAbFlag() + alarmData.getAttrIndex();
        synchronized (assetId.intern()) {
            if (alarmData.getLinkStatus().equals(LinkStatusEnum.UP.name())) {
                // 状态为连接


                CreateEventReq eventReq = new CreateEventReq();

                String occurTime = alarmData.getOccurTime();
                if (StrUtil.isNotEmpty(occurTime)) {
                    eventReq.setCreateTime(DateUtil.parse(occurTime, "yyyy-MM-dd HH:mm:ss"));
                } else {
                    eventReq.setCreateTime(new Date());
                }
                // 事件性质
                eventReq.setEventLevel(EventLevelEnum.NORMAL.getCode());
                String format = String.format("业务告警-卡斯柯连接变化：实体号为%s的设备恢复连接", alarmData.getEntityId());
                eventReq.setOriginalMsg(format);

                eventReq.setAssetId(assetId);
                eventReq.setUniqueCode(EventUniqueCode.CASCO_LINK);
                // 使用说明见 AlarmEvent.flag
                eventReq.setFlag(EventGroupConstant.CACSON_LINK + alarmData.getAttrGroupId() + alarmData.getAttrIndex());
                eventReq.setGroupFlag(EventGroupConstant.CACSON_LINK);
                eventReq.setBusinessType(BusinessTypeEnums.CASCO.code);


                try {
                    eventLogicServ.addEvent(eventReq);
                } catch (Exception e) {
                    log.error("处理casco连接事件异常：{}", e.getMessage(), e);
                }

                return;
            }
            // 状态为断开
            CreateEventReq eventReq = new CreateEventReq();
            String occurTime = alarmData.getOccurTime();
            if (StrUtil.isNotEmpty(occurTime)) {
                eventReq.setCreateTime(DateUtil.parse(occurTime, "yyyy-MM-dd HH:mm:ss"));
            } else {
                eventReq.setCreateTime(new Date());
            }

            // 事件性质
            eventReq.setEventLevel(EventLevelEnum.ABNORMAL.getCode());
            String format = String.format("业务告警-卡斯柯连接变化：实体号为%s的设备连接中断", alarmData.getEntityId());
            eventReq.setOriginalMsg(format);

            eventReq.setAssetId(assetId);
            eventReq.setUniqueCode(EventUniqueCode.CASCO_LINK);
            // 使用说明见 AlarmEvent.flag
            eventReq.setFlag(EventGroupConstant.CACSON_LINK + alarmData.getAttrGroupId() + alarmData.getAttrIndex());
            eventReq.setGroupFlag(EventGroupConstant.CACSON_LINK);

            try {
                eventLogicServ.addEvent(eventReq);
            } catch (Exception e) {
                log.error("处理casco连接事件异常：{}", e.getMessage(), e);
            }
        }
    }

}
