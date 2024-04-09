package com.jcca.component.other;

import com.jcca.common.utils.AppPattenUtils;
import com.jcca.common.utils.MyIdUtil;
import com.jcca.component.dto.ReceiveAlarmDto;
import com.jcca.component.enums.ReceiveAlarmTypeEnum;
import com.jcca.component.event.EventLogicService;
import com.jcca.component.event.bean.CreateEventReq;
import com.jcca.component.event.constant.EventUniqueCode;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.asset.service.AssetService;
import com.jcca.web.event.enums.EventLevelEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.Objects;

/**
 * @ClassName RouterTableAlarmService
 * @Description 路由表告警
 * @Date 2020/7/1 14:37
 * @Author hanwone
 */
@Service
@Slf4j
@Transactional(rollbackFor = Exception.class)
public class RouterTableAlarmService implements AlarmAdapter {
    @Resource
    private AssetService assetService;
    @Resource
    private EventLogicService eventLogicServ;

    @Override
    public String getCode() {
        return ReceiveAlarmTypeEnum.ROUTER_TABLE.getCode();
    }

    @Override
    public void handle(ReceiveAlarmDto alarmData) {
        Asset asset = assetService.findOneByIp(alarmData.getAssetIp());
        if (Objects.isNull(asset)) {
            log.error("IP为[{}]的资产不存在", alarmData.getAssetIp());
            return;
        }
        Byte noWatch = 0;
        if (Objects.isNull(asset) || noWatch.equals(asset.getWatch())) {
            log.info("【路由表告警非监控资产，不推送】：{},【监控状态】:{}", asset.getIp(), asset.getWatch());
            return;
        }

        String id = asset.getId();
        String lockKey = "ADD_EVENT_ROUTER_" + id;
        synchronized (lockKey.intern()) {
            CreateEventReq eventReq = new CreateEventReq();

            String removeDateStr = AppPattenUtils.removeDateStr(alarmData.getContent());

            eventReq.setAssetId(id);

            eventReq.setEventLevel(EventLevelEnum.NOTIFY.getCode());
            eventReq.setUniqueCode(EventUniqueCode.UNKONW_EVENT);
            eventReq.setOriginalMsg(alarmData.getContent());
            eventReq.setFlag(removeDateStr);
            eventReq.setGroupFlag(MyIdUtil.getId());
            eventReq.setCreateTime(new Date());

            try {
                eventLogicServ.addEvent(eventReq);
            } catch (Exception e) {
                log.error("处理路由表事件异常：{}", e.getMessage(), e);
            }
        }
    }
}
