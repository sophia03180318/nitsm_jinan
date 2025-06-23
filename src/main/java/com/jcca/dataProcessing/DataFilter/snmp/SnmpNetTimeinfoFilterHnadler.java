package com.jcca.dataProcessing.DataFilter.snmp;

import cn.hutool.core.util.NumberUtil;
import com.jcca.admin.system.entity.SysModuleConfig;
import com.jcca.admin.system.service.SysModuleConfigService;
import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.redis.service.RedisService;
import com.jcca.common.utils.AppLogUtils;
import com.jcca.common.utils.EncodeUtils;
import com.jcca.dataProcessing.Entity.ChangeInfo;
import com.jcca.dataProcessing.Entity.SnmpEventInfoEntity;
import com.jcca.dataProcessing.enums.StatusInfoChangeTypeEnum;
import com.jcca.dataProcessing.manager.IEventInfoManagerService;
import com.jcca.dataProcessing.manager.bean.AlarmTempReq;
import com.jcca.dataProcessing.support.IEvent;
import com.jcca.dataProcessing.support.IFilterHandler;
import com.jcca.web.event.enums.EventLevelEnum;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author Zhaozheng
 * @description TODO
 * @className SnmpIBMinfoFilterHnadler
 * @date 2023/11/28 11:59
 * @since 2.1.0.0
 */

/**
 * IBM设备snmp告警信息
 * .1.3.6.1.4.1.2.6.158.5.1.1   "Timestamp of Local Date and Time when alert was generated"
 * .1.3.6.1.4.1.2.6.158.5.1.3     "SP System Identification - Text Identification"
 * .1.3.6.1.4.1.2.6.158.5.1.5     "Host System UUID(Universal Unique ID)"
 * .1.3.6.1.4.1.2.6.158.5.1.6      "Host System Serial Number"
 * .1.3.6.1.4.1.2.6.158.5.1.8      "Alert Severity Value - Critical Alert(0)  - Non-Critical Alert(2) - System Alert(4)- Recovery(8)"
 * .1.3.6.1.4.1.2.6.158.5.1.9        "Alert Message Text"
 * .1.3.6.1.4.1.2.6.158.5.1.10        "Alert Message ID"
 * .1.3.6.1.4.1.2.6.158.5.1.11        "Alert Message ID"
 * .1.3.6.1.4.1.2.6.158.5.1.12        "Host Contact"
 * .1.3.6.1.4.1.2.6.158.5.1.13        "Host Location"
 */
@Component("snmpNetTimeinfoFilterHnadler")
public class SnmpNetTimeinfoFilterHnadler extends IFilterHandler<SnmpEventInfoEntity> {

    @Resource
    private IEventInfoManagerService eventInfoChangeManagerService;
    @Resource
    private RedisService redisService;
    private Map<String, ChangeInfo> mapCache = new HashMap<>();
    private ScheduledExecutorService scheduledExecutorService;

    private Long internal = null;

    @Resource
    private SysModuleConfigService configService;

    @Override
    public boolean handler(SnmpEventInfoEntity info) {

        String processName = null;
        //判断map中是否包含NetTimeService关键字的进程
        for (String key : info.getMap().keySet()) {
            String str = info.getMap().get(key);
            if (str != null && str.contains("NetTimeService.exe")) {
                processName = str;
                break;
            }
        }
        if (processName != null && processName.contains("NetTimeService.exe")) {
            String message = EncodeUtils.parseHexStr(info.getMap().get("1.3.6.1.4.1.311.1.13.1.9999.1.0"));

            String redisKey = info.getAssetIp() + ":" + info.getAssetId() + ":" + StatusInfoChangeTypeEnum.status_time_server.getCode();
            String mapKey = StatusInfoChangeTypeEnum.status_time_server_status.getCode();
            String mapKey1 = StatusInfoChangeTypeEnum.status_time_server_location.getCode();
            String mapKey2 = StatusInfoChangeTypeEnum.status_time_server_message.getCode();
            ChangeInfo changeInfo = new ChangeInfo();
            changeInfo.setValue(EventLevelEnum.NORMAL.getCode());
            changeInfo.setRedisKey(redisKey);
            changeInfo.setCollectTime(new Date());
            changeInfo.setMapKey(mapKey);
            info.getMaps().put(mapKey, changeInfo);


            ChangeInfo changeInfo1 = new ChangeInfo();
            changeInfo1.setValue(processName);
            changeInfo1.setCollectTime(new Date());
            changeInfo1.setRedisKey(redisKey);
            changeInfo1.setMapKey(mapKey1);
            info.getMaps().put(mapKey1, changeInfo1);

            ChangeInfo changeInfo2 = new ChangeInfo();
            changeInfo2.setValue(message);
            changeInfo2.setRedisKey(redisKey);
            changeInfo2.setMapKey(mapKey2);
            changeInfo2.setCollectTime(new Date());
            info.getMaps().put(mapKey2, changeInfo2);

            this.netTimeConfire(); //配置监控间隔
            if (internal == null) {
                return true;
            }
            String eventRedisKey = StatusInfoChangeTypeEnum.event_clock_state.getCode();
            String eventMapKey = changeInfo.getMapKey();
            Integer status = EventLevelEnum.NORMAL.getCode();

            String str = status == EventLevelEnum.ABNORMAL.getCode() ? "异常。" : "恢复。";
            String descStr = String.format(StatusInfoChangeTypeEnum.event_clock_state.getDescr(), str);

            AlarmTempReq tempReq = new AlarmTempReq();
            tempReq.setAssetIp(info.getAssetIp());
            tempReq.setOrgMsg(descStr);

            //是否会有恢复事件产生
            IEvent event = eventInfoChangeManagerService.creatChangeEvent(info.getAssetId(), changeInfo, eventRedisKey, eventMapKey, status,tempReq,info.getInspectRecordId());
            if (event != null) {
                //被事件信息截取
                changeInfo.setIsEvent(true);
                event.setDescStr(descStr);
                this.dispatureEvent(event);
            }
            String key = info.getAssetIp() + ":" + info.getAssetId() + ":" + StatusInfoChangeTypeEnum.status_time_server_monitor.getCode();
            redisService.set(key, internal + 10, internal + 10); //5分钟分钟失效
            mapCache.put(key, changeInfo);

            //说明是第一次收到时钟同步信息
            if (scheduledExecutorService == null) {
                scheduledExecutorService = Executors.newScheduledThreadPool(1);
                scheduledExecutorService.scheduleAtFixedRate(this::eventHandler, 0, internal, TimeUnit.SECONDS);
            }

            return false;
        }
        return true;
    }

    private void netTimeConfire() {
//        config:tk-net-time
        SysModuleConfig tkNetTimeConfig = configService.getSysModuleConfig("config:tk-net-time");
        if (Objects.nonNull(tkNetTimeConfig) && NumberUtil.isLong(tkNetTimeConfig.getValue())) {
            if (internal != null && internal != Long.parseLong(tkNetTimeConfig.getValue())) {
                //如果配置变了
                if (scheduledExecutorService != null) {
                    //将定时器关闭
                    scheduledExecutorService.shutdown();
                    scheduledExecutorService = null;
                }
            }
            internal = Long.parseLong(tkNetTimeConfig.getValue());

        } else {
            //如果配置设置取消
            if (scheduledExecutorService != null) {
                //将定时器关闭
                scheduledExecutorService.shutdown();
                scheduledExecutorService = null;
            }
            AppLogUtils.buildLogInfo(LogFunctionEnum.ASSET_MANAGE, internal, "铁科时钟同步监控时间间隔未配置");
        }
    }


    @Override
    public boolean isNeedNexthandle(Boolean flag) {
        return flag;
    }


    public void eventHandler() {
        netTimeConfire();
        AppLogUtils.buildLogInfo(LogFunctionEnum.DATA_PROCESS, internal, "铁科时钟同步:监控校验");
        for (Map.Entry<String, ChangeInfo> entry : mapCache.entrySet()) {
            String key = entry.getKey();
            String[] strs = key.split(":");
            String monitorKey = strs[0] + ":" + strs[1] + ":" + StatusInfoChangeTypeEnum.status_time_server_monitor.getCode();
            if (!redisService.exists(monitorKey)) {
                ChangeInfo changeInfo = entry.getValue();
                changeInfo.setValue(EventLevelEnum.ABNORMAL.getCode());
                changeInfo.setCollectTime(new Date());
                String eventRedisKey = StatusInfoChangeTypeEnum.event_clock_state.getCode();
                String eventMapKey = changeInfo.getMapKey();
                Integer status = EventLevelEnum.ABNORMAL.getCode();
                String str = status == EventLevelEnum.ABNORMAL.getCode() ? "异常。" : "恢复。";
                String descStr = String.format(StatusInfoChangeTypeEnum.event_clock_state.getDescr(), str);

                AlarmTempReq tempReq = new AlarmTempReq();
                tempReq.setOrgMsg(descStr);

                IEvent event = eventInfoChangeManagerService.creatChangeEvent(strs[1], changeInfo, eventRedisKey, eventMapKey, status,tempReq,null);
                if (event != null) {
                    //被事件信息截取
                    changeInfo.setIsEvent(true);
                    event.setDescStr(descStr);
                    this.dispatureEvent(event);
                }
            }
        }
    }


}
