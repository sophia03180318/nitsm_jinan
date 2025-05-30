package com.jcca.component.quartz.alarm.impl;

import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.utils.AppLogUtils;
import com.jcca.common.utils.TestIpUtil;
import com.jcca.component.quartz.alarm.AlarmJobService;
import com.jcca.dataProcessing.Entity.ChangeInfo;
import com.jcca.dataProcessing.enums.StatusInfoChangeTypeEnum;
import com.jcca.dataProcessing.manager.IEventInfoManagerService;
import com.jcca.dataProcessing.manager.bean.AlarmTempReq;
import com.jcca.dataProcessing.support.IEvent;
import com.jcca.dataProcessing.support.ListenerManager;
import com.jcca.web.asset.entity.Asset;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Date;


/**
 * 告警任务接口实现
 */
@Slf4j
@Service
public class AlarmJobServiceImpl extends ListenerManager implements AlarmJobService {


    @Resource
    private IEventInfoManagerService eventInfoChangeManagerService;


    @Override
    public void pingIpmiPort(Asset asset) {
        String ipmiIp = asset.getIpmiIp();
        Boolean ping = null;
        try {
            ping = TestIpUtil.ping(ipmiIp, 5);
        } catch (IOException e) {
            AppLogUtils.buildLogError(LogFunctionEnum.CRON_DATA_MANAGER_PORT,ipmiIp,e);
            return ;
        }

        String eventMapKey = asset.getIp() + "_" + asset.getId();

        ChangeInfo changeInfo = new ChangeInfo();
        changeInfo.setIsEvent(true);
        changeInfo.setCollectTime(new Date());

        AlarmTempReq alarmTempReq = new AlarmTempReq();

        if(!ping){
            String msg = String.format("管理口: %s 与 综合维护平台网络已断开！", asset.getIpmiIp());
            alarmTempReq.setOrgMsg(msg);
            alarmTempReq.setCollectValue("断开");
            //失败 上失败事件
            IEvent event = eventInfoChangeManagerService.creatChangeEvent(asset.getId(), changeInfo, StatusInfoChangeTypeEnum.event_ipmi_ping.getCode(), eventMapKey, -1,alarmTempReq,null);
            event.setDescStr(msg);
            this.dispatureEvent(event);
        }else {
            String msg = String.format( "管理口: %s 与 综合维护平台网络已连通", asset.getIpmiIp());
            //成功 上成功事件
            alarmTempReq.setOrgMsg(msg);
            alarmTempReq.setCollectValue("联通");
            IEvent event = eventInfoChangeManagerService.creatChangeEvent(asset.getId(), changeInfo, StatusInfoChangeTypeEnum.event_ipmi_ping.getCode(), eventMapKey, 1,alarmTempReq,null);
            event.setDescStr(msg);
            this.dispatureEvent(event);

        }

    }

}
