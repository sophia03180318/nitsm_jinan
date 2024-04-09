package com.jcca.component.other;

import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONUtil;
import com.jcca.component.dto.ReceiveAlarmDto;
import com.jcca.component.enums.ReceiveAlarmTypeEnum;
import com.jcca.component.event.EventLogicService;
import com.jcca.component.event.bean.CreateEventReq;
import com.jcca.component.event.constant.EventGroupConstant;
import com.jcca.component.event.constant.EventUniqueCode;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.asset.service.AssetService;
import com.jcca.web.event.enums.EventLevelEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * @ClassName DBAlarmService
 * @Description 数据库告警处理
 * @Date 2020/08/04 16:32
 * @Author hanwone
 */
@Service
@Slf4j
@Transactional(rollbackFor = Exception.class)
public class DBAlarmService implements AlarmAdapter {

    @Resource
    private AssetService assetService;
    @Resource
    private EventLogicService eventServ;

    @Override
    public String getCode() {
        return ReceiveAlarmTypeEnum.ORACLE_DB.getCode();
    }

    @Override
    public void handle(ReceiveAlarmDto alarmData) {
        String assetIp = alarmData.getAssetIp();
        Asset asset = assetService.findOneByIp(assetIp);
        if (Objects.isNull(asset)) {
            log.error("IP为[{}]的资产不存在", assetIp);
            return;
        }

        log.info("【收到ORACLE数据库连接通断数据】：" + JSONUtil.toJsonStr(alarmData));

        String lockKey = "ADD_EVENT_DB_" + assetIp;
        synchronized (lockKey.intern()) {
            if (Objects.isNull(alarmData.getFlag())) {
                return;
            }

            // 数据库连接事件
            CreateEventReq req = new CreateEventReq();
            req.setAssetId(asset.getId());
            req.setCreateTime(DateUtil.parse(alarmData.getOccurTime(), "yyyy-MM-dd HH:mm:ss"));
            req.setFlag("DB");

            String originalMsg = "系统测试数据库连接，连接失败！";

            if (alarmData.getFlag()) {
                originalMsg = "系统测试数据库连接，连接成功！";
                req.setEventLevel(EventLevelEnum.NORMAL.getCode());
            } else {
                req.setEventLevel(EventLevelEnum.ABNORMAL.getCode());
            }

            req.setOriginalMsg(originalMsg);
            req.setUniqueCode(EventUniqueCode.DB_LINK);
            req.setGroupFlag(EventGroupConstant.DB);

            try {
                eventServ.addEvent(req);
            } catch (Exception e) {
                log.error("数据库添加事件异常：{}", e.getMessage(), e);
            }

        }

    }
}
