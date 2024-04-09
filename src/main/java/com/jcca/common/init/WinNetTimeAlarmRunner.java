package com.jcca.common.init;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.json.JSONUtil;
import com.jcca.admin.system.entity.SysModuleConfig;
import com.jcca.common.bean.constant.GlobalConfigConst;
import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.redis.service.RedisService;
import com.jcca.common.utils.AppLogUtils;
import com.jcca.common.utils.MyIdUtil;
import com.jcca.component.event.EventLogicService;
import com.jcca.component.event.bean.CreateEventReq;
import com.jcca.component.event.constant.EventUniqueCode;
import com.jcca.web.event.enums.EventLevelEnum;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author hanhw
 * @description windows时钟同步服务器同步间隔过长告警监控
 * @className WinNetTimeAlarmRunner
 * @date 2023/4/7 23:08
 * @since 2.0.3.0
 */
@Order(4)
@Component
public class WinNetTimeAlarmRunner implements ApplicationRunner {

    @Resource
    private RedisService redisService;
    @Resource
    private EventLogicService eventLogicServ;

    @Override
    public void run(ApplicationArguments args) {
        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
        scheduledExecutorService.scheduleAtFixedRate(this::dosomething, 0, 5, TimeUnit.SECONDS);
    }

    private void dosomething() {
        Object o = redisService.get(GlobalConfigConst.LAST_TK_NET_TIME_SERVICE);
        if (Objects.isNull(o)) {
            return;
        }
        long internal = 30; // 默认间隔30秒
        SysModuleConfig tkNetTimeConfig = (SysModuleConfig) redisService.get(GlobalConfigConst.TK_NET_TIME_SERVICE);
        if (Objects.nonNull(tkNetTimeConfig) && NumberUtil.isInteger(tkNetTimeConfig.getValue())) {
            internal = Integer.parseInt(tkNetTimeConfig.getValue());
        } else {
            AppLogUtils.buildLogWarn(LogFunctionEnum.CRON_NETTIMESERVICE, "config:tk-net-time", "铁科时钟同步间隔未配置或格式不对,使用默认间隔30秒");
        }

        Map mapMap = JSONUtil.toBean(o.toString(), Map.class);
        Set set = mapMap.keySet();
        long now = new Date().getTime() / 1000;
        long lastTime = 0;
        String cont = "", flag;
        for (Object assetId : set) {
            Map map = (Map) mapMap.get(assetId);
            lastTime = Long.parseLong(map.get("lastTime").toString());
            cont = map.get("content").toString();
            flag = map.get("flag").toString();
            if (now - lastTime > internal) {
                if (Objects.equals(flag, "1")) {
                    CreateEventReq eventReq = new CreateEventReq();
                    eventReq.setEventLevel(EventLevelEnum.ABNORMAL.getCode());
                    eventReq.setAssetId(assetId.toString());
                    eventReq.setUniqueCode(EventUniqueCode.SNMP_NOTIFY_UNIQUE_CODE);
                    eventReq.setOriginalMsg(cont);
                    eventReq.setFlag("SNMP");
                    eventReq.setGroupFlag(MyIdUtil.getId());
                    eventReq.setCreateTime(new Date());
                    try {
                        eventLogicServ.addEvent(eventReq);
                        AppLogUtils.buildLogInfo(LogFunctionEnum.CRON_NETTIMESERVICE, "铁科设备[" + assetId + "]时钟同步间隔时间" + (now - lastTime) + "秒", "");
                    } catch (Exception e) {
                        AppLogUtils.buildLogError(LogFunctionEnum.CRON_NETTIMESERVICE, "处理铁科时钟同步SNMP事件异常", e);
                    }
                    map.put("flag", "0");
                }
            } else {
                map.put("flag", "1");
            }
            mapMap.put(assetId, map);
            redisService.set(GlobalConfigConst.LAST_TK_NET_TIME_SERVICE, JSONUtil.toJsonStr(mapMap));
        }
    }
}
