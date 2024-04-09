package com.jcca.component.other;

import cn.hutool.json.JSONUtil;
import com.jcca.admin.system.entity.SysModuleConfig;
import com.jcca.admin.system.service.SysModuleConfigService;
import com.jcca.common.bean.constant.GlobalConfigConst;
import com.jcca.common.redis.service.RedisService;
import com.jcca.common.utils.MyIdUtil;
import com.jcca.component.dto.ReceiveAlarmDto;
import com.jcca.component.event.EventLogicService;
import com.jcca.component.event.bean.CreateEventReq;
import com.jcca.component.event.constant.EventUniqueCode;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.asset.service.AssetService;
import com.jcca.web.asset.utils.enums.AssetWatchStatusEnum;
import com.jcca.web.event.enums.EventLevelEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @ClassName SnmpAlarmService
 * @Description snmp告警处理
 * @Date 2020/6/22 14:39
 * @Author hanwone
 */
@Service
@Slf4j
@Transactional(rollbackFor = Exception.class)
public class SnmpEventService {

    @Resource
    private AssetService assetService;
    @Resource
    private EventLogicService eventLogicServ;
    @Resource
    private RedisService redisService;
    @Resource
    private SysModuleConfigService sysModuleConfigService;

    public void addEvent(ReceiveAlarmDto alarmData) {
        log.info("-------------------处理snmp告警-------------------");
        SysModuleConfig snmpSwitch = sysModuleConfigService.getSysModuleConfig(GlobalConfigConst.SNMP_SWITCH);
        if (Objects.isNull(snmpSwitch) || "0".equals(snmpSwitch.getValue())) {
            log.error("===============未配置接收snmp开关=================");
            return;
        }

        Asset asset = assetService.getOneByAllIp(alarmData.getAssetIp());
        if (Objects.isNull(asset)) {
            log.error("处理SNMP告警失败：IP为[{}]的资产不存在", alarmData.getAssetIp());
            return;
        }

        if (AssetWatchStatusEnum.WATCH_STATUS_NO.getCode() == asset.getWatch()) {
            log.error("IP为[{}]的资产不监控，不接收SNMP通知", alarmData.getAssetIp());
            return;
        }

        String id = asset.getId();
        // 铁科时钟同步告警逻辑处理 20230406 hanhw
        this.parseContent(alarmData);
        if (alarmData.getContent().contains("NetTimeService")) {
            log.info("ITSM收到铁科时钟程序【NetTimeService】消息，IP：{}", alarmData.getAssetIp());
            boolean tk = this.handleTkNettime(id, alarmData.getContent());
            if (tk) return;
        }

        String Key = "ADD_EVENT_SNMP_" + id;
        synchronized (Key.intern()) {
            String content = alarmData.getContent();
            CreateEventReq eventReq = new CreateEventReq();

            eventReq.setEventLevel(EventLevelEnum.NOTIFY.getCode());
            eventReq.setAssetId(id);
            eventReq.setUniqueCode(EventUniqueCode.SNMP_NOTIFY_UNIQUE_CODE);

            eventReq.setOriginalMsg(content);
            eventReq.setFlag("SNMP");
            eventReq.setGroupFlag(MyIdUtil.getId());
            eventReq.setCreateTime(new Date());

            try {
                eventLogicServ.addEvent(eventReq);
            } catch (Exception e) {
                log.error("处理SNMP事件异常：{}", e.getMessage(), e);
            }
        }

    }

    private void parseContent(ReceiveAlarmDto alarmData) {
        String cont = alarmData.getContent();
        if (cont.contains("NetTimeService")) {
            return;
        }

        String[] dohaoArr = cont.split(",");
        for (int j = dohaoArr.length - 1; j > 0; j--) {
            String[] denghaoArr = dohaoArr[j].split("=");
            if (denghaoArr.length > 1) {
                String hex = denghaoArr[1].replaceAll(":", "");
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < hex.length() - 1; i += 2) {
                    String output = hex.substring(i, (i + 2));
                    if (output.length() < 2) continue;
                    int decimal = Integer.parseInt(output, 16);
                    sb.append((char) decimal);
                }
                if (sb.toString().contains("NetTimeService")) {
                    alarmData.setContent(sb.toString());
                    break;
                }
            }
        }
    }

    private Boolean handleTkNettime(String assetId, String cont) {
        Object o = redisService.get(GlobalConfigConst.LAST_TK_NET_TIME_SERVICE);
        if (Objects.isNull(o)) {
            Map<String, Map<String, String>> mapMap = new HashMap<>();
            Map<String, String> map = new HashMap<>();
            map.put("assetId", assetId);
            map.put("lastTime", (new Date().getTime() / 1000) + "");
            map.put("content", cont);
            map.put("flag", "1");

            mapMap.put(assetId, map);
            redisService.set(GlobalConfigConst.LAST_TK_NET_TIME_SERVICE, JSONUtil.toJsonStr(mapMap));
        } else {
            Map mapMap = JSONUtil.toBean(o.toString(), Map.class);
            Object m = mapMap.get(assetId);
            if (Objects.nonNull(m)) {
                Map<String, String> map = (Map) m;
                map.put("lastTime", (new Date().getTime() / 1000) + "");
                map.put("content", cont);
                mapMap.put(assetId, map);
            } else {
                Map<String, String> map = new HashMap<>();
                map.put("assetId", assetId);
                map.put("lastTime", (new Date().getTime() / 1000) + "");
                map.put("content", cont);
                map.put("flag", "1");
                mapMap.put(assetId, map);
            }
            redisService.set(GlobalConfigConst.LAST_TK_NET_TIME_SERVICE, JSONUtil.toJsonStr(mapMap));
        }
        return true;
    }
}
