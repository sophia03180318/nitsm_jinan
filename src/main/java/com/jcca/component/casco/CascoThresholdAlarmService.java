package com.jcca.component.casco;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.jcca.common.enums.BusinessTypeEnums;
import com.jcca.common.utils.AppMathUtil;
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
 * @ClassName CascoThresholdAlarmService
 * @Description 处理卡斯科阈值告警
 * @Author oamwh
 * @Date 2021/1/7 9:48
 * @Version ITSM2.0
 **/
//@Service
@Slf4j
public class CascoThresholdAlarmService implements BrokerAlarmAdapter {


    @Resource
    private EventLogicService eventLogicServ;


    @Override
    public String getCode() {
        // 适配枚举ReceiveAlarmTypeEnum的code
        return ReceiveAlarmTypeEnum.CASCO_THRESHOLD.getCode();
    }

    @Override
    public void handle(ItsmQueueReq alarmData) {
        String assetId = alarmData.getAssetId();
        if (StrUtil.isBlank(assetId)) {
            log.info("收到卡斯科告警资产ID为空，收到数据为：{}", JSONUtil.toJsonStr(alarmData));
        }
        log.info("开始处理卡斯柯阈值告警,收到的数据为:{}", alarmData.toString());
        synchronized (assetId.intern()) {
            // 采集值大于设定值 true
            Boolean compare = AppMathUtil.compare(alarmData.getCollectValue(), alarmData.getBaseValue());


            CreateEventReq eventReq = new CreateEventReq();

            if (compare) {
                eventReq.setEventLevel(EventLevelEnum.ABNORMAL.getCode());
                String format = String.format("业务告警-卡斯柯采集到属性索引：%s，采集值 %s,设定值：%s ,超阈值！", alarmData.getAttrIndex(), alarmData.getCollectValue(), alarmData.getBaseValue());
                eventReq.setOriginalMsg(format);
            } else {
                eventReq.setEventLevel(EventLevelEnum.NORMAL.getCode());
                String format = String.format("业务告警-卡斯柯采集值：%s ,设定值：%s ,指标正常！", alarmData.getCollectValue(), alarmData.getBaseValue());
                eventReq.setOriginalMsg(format);
            }
            eventReq.setAssetId(assetId);
            eventReq.setBaseValue(alarmData.getBaseValue());
            eventReq.setCollectValue(alarmData.getCollectValue());
            // 事件匹配码
            eventReq.setUniqueCode(EventUniqueCode.CASCO_THRE);
            eventReq.setFlag(EventUniqueCode.CASCO_THRE + alarmData.getAttrGroupId() + alarmData.getAttrIndex());
            eventReq.setGroupFlag(EventGroupConstant.CACSON_TH);
            eventReq.setBusinessType(BusinessTypeEnums.CASCO.code);

            String occurTime = alarmData.getOccurTime();
            if (StrUtil.isNotEmpty(occurTime)) {
                eventReq.setCreateTime(DateUtil.parse(occurTime, "yyyy-MM-dd HH:mm:ss"));
            } else {
                eventReq.setCreateTime(new Date());
            }

            try {
                eventLogicServ.addEvent(eventReq);
            } catch (Exception e) {
                log.error("处理casco阈值事件异常：{}", e.getMessage(), e);
            }
        }

    }
}
