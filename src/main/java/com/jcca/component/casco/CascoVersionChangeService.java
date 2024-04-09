package com.jcca.component.casco;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.jcca.common.enums.BusinessTypeEnums;
import com.jcca.component.dto.ItsmQueueReq;
import com.jcca.component.enums.ReceiveAlarmTypeEnum;
import com.jcca.component.event.EventLogicService;
import com.jcca.component.event.bean.CreateEventReq;
import com.jcca.component.event.constant.EventGroupConstant;
import com.jcca.component.event.constant.EventUniqueCode;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.asset.service.AssetService;
import com.jcca.web.construction.service.ConstructionRecordService;
import com.jcca.web.event.enums.EventLevelEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.Objects;

/**
 * @author syt
 * @ClassName CascoVersionChangeService
 * @Description 卡斯柯版本变更
 * @date 2021/4/14/014  10:29
 * @Version ITSM2.0
 */
//@Service
@Slf4j
public class CascoVersionChangeService implements BrokerAlarmAdapter {

    // 资产
    @Resource
    private AssetService assetService;
    // 施工记录
    @Resource
    private ConstructionRecordService constructionRecordService;
    @Resource
    private EventLogicService eventLogicServ;

    @Override
    public String getCode() {
        // 适配枚举ReceiveAlarmTypeEnum的code
        return ReceiveAlarmTypeEnum.CASCO_VERSION_CHANGE.getCode();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handle(ItsmQueueReq alarmData) {
        if (alarmData.getOldVersion().equals(alarmData.getNowVersion())) {
            // 当前版本与变更版本相同 不产生告警
            log.info("卡斯柯版本无变化,之前版本{},当前版本{}", alarmData.getOldVersion(), alarmData.getNowVersion());
            return;
        }
        log.info("开始处理卡斯柯版本变更告警,收到的信息为:{}", alarmData.toString());
        String assetId = alarmData.getAssetId();
        // itsm资产信息
        Asset asset = assetService.getById(assetId);
        if (Objects.isNull(asset)) {
            log.info("版本变更未查询到资产,资产ID为{}", assetId);
            return;
        }


        synchronized (assetId.intern()) {
            CreateEventReq eventReq = new CreateEventReq();

            // 事件性质
            eventReq.setEventLevel(EventLevelEnum.NOTIFY.getCode());
            String format = String.format("业务告警-卡斯柯软件:%s 版本变化：新版本%s，老版本%s", alarmData.getCascoSoftName(), alarmData.getNowVersion(), alarmData.getOldVersion());
            eventReq.setOriginalMsg(format);

            eventReq.setAssetId(assetId);
            eventReq.setBaseValue(alarmData.getOldVersion());
            eventReq.setCollectValue(alarmData.getNowVersion());
            eventReq.setUniqueCode(EventUniqueCode.CASCO_VERSION);
            String alarmCode =
                    +alarmData.getEntityId() + "-"
                            + alarmData.getAbFlag() + "-"
                            + alarmData.getNowVersion();
            eventReq.setFlag(alarmCode);
            eventReq.setGroupFlag(EventGroupConstant.CACSON_VERSION);
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
                log.error("处理casco版本事件异常：{}", e.getMessage(), e);
            }

        }


    }

}
