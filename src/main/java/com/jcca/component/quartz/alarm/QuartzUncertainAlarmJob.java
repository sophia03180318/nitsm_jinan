package com.jcca.component.quartz.alarm;


import com.jcca.admin.system.entity.SysModuleConfig;
import com.jcca.admin.system.service.SysModuleConfigService;
import com.jcca.common.input.LogInputUtils;
import com.jcca.common.input.ServerTypeEnum;
import com.jcca.common.utils.MyIdUtil;
import com.jcca.dataProcessing.Entity.ChangeInfo;
import com.jcca.dataProcessing.enums.StatusInfoChangeTypeEnum;
import com.jcca.dataProcessing.listener.EventInfoListener;
import com.jcca.dataProcessing.manager.IEventInfoManagerService;
import com.jcca.dataProcessing.manager.bean.AlarmTempReq;
import com.jcca.dataProcessing.support.IEvent;
import com.jcca.dataProcessing.support.ListenerManager;
import com.jcca.web.alarm.entity.AlarmInfo;
import com.jcca.web.alarm.service.AlarmInfoService;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.asset.service.AssetService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 几天内未确认的告警
 *
 * @author sophia
 */
@Slf4j
@Service
@DisallowConcurrentExecution
public class QuartzUncertainAlarmJob extends QuartzJobBean {

    @Resource
    private AlarmInfoService alarmInfoService;
    @Resource
    private SysModuleConfigService configService;
    @Resource
    private AssetService assetServ;

    @Resource
    private IEventInfoManagerService eventInfoChangeManagerService;

    private ListenerManager listenerManager;

    /**
     * 系统启动第一次任务跳过
     */
    private static boolean isOnce = true;


    @PostConstruct
    public void init() {
        if(Objects.isNull(listenerManager)){
            listenerManager = new ListenerManager();
            listenerManager.addDataSourceListener(new EventInfoListener());
        }
    }


    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        if (LogInputUtils.inputInfo(ServerTypeEnum.JOB_QUARTZ)) {
            log.info("定时任务-开始巡检系统内是否有长时间未确认告警");
        }

        if(isOnce){
            isOnce = false;
            return ;
        }

        this.exeJob();
    }

    /**
     * 执行
     */
    public void exeJob(){
        SysModuleConfig config = configService.getSysModuleConfig("config:uncertainAlarmDay");
        if (Objects.isNull(config)) {
            SysModuleConfig config1 = new SysModuleConfig();
            config1.setId(MyIdUtil.getId());
            config1.setName("config:uncertainAlarmDay");
            config1.setValue("99:99");
            config1.setDescription("x:y->x天内未确认未恢复的二级告警;y天内未恢复的三级告警");
            config1.setOrgId("0");
            config1.setServiceType(3);
            configService.save(config1);
            config = config1;
        }
        int days_2 = 3;
        int days_3 = 7;
        String[] days = config.getValue().split(":");
        if (days.length == 2) {
            try {
                days_2 = Integer.parseInt(days[0]);
                days_3 = Integer.parseInt(days[1]);
            } catch (NumberFormatException e) {
                log.error("config:uncertainAlarmDay 读取出错 请检查~");
            }
        }

        List<AlarmInfo> level_2 = alarmInfoService.findUnconfirmAlarm(2, days_2);
        if (Objects.nonNull(level_2) && !level_2.isEmpty()) {
            Map<String, List<String>> assetAndAlarm = level_2.stream().collect(Collectors.groupingBy(AlarmInfo::getAssetId, Collectors.mapping(AlarmInfo::getTitle, Collectors.toList())));
            for (Map.Entry<String, List<String>> kv : assetAndAlarm.entrySet()) {
                Asset asset = assetServ.getById(kv.getKey());
                if(Objects.isNull(asset)){
                    continue ;
                }
                String eventMapKey = asset.getIp() + "_" + asset.getId();
                ChangeInfo changeInfo = new ChangeInfo();
                changeInfo.setIsEvent(true);
                changeInfo.setCollectTime(new Date());
                AlarmTempReq alarmTempReq = new AlarmTempReq();
                alarmTempReq.setOrgMsg("资产存在" + days_2 + "天内未确认的二级告警:" + kv.getValue().toString());
                alarmTempReq.setCollectValue(days_2+"天");
                IEvent event = eventInfoChangeManagerService.creatChangeEvent(asset.getId(), changeInfo, StatusInfoChangeTypeEnum.event_unconfirmed_2.getCode(), eventMapKey, -1,alarmTempReq,null,"");
                event.setDescStr("资产存在" + days_2 + "天内未确认的二级告警:" + kv.getValue().toString());
                listenerManager.dispatureEvent(event);
            }
        }


        List<AlarmInfo> level_3 = alarmInfoService.findUnconfirmAlarm(3, days_3);
        if (Objects.nonNull(level_3) && !level_3.isEmpty()) {
            Map<String, List<String>> assetAndAlarm = level_3.stream().collect(Collectors.groupingBy(AlarmInfo::getAssetId, Collectors.mapping(AlarmInfo::getTitle, Collectors.toList())));
            for (Map.Entry<String, List<String>> kv : assetAndAlarm.entrySet()) {
                Asset asset = assetServ.getById(kv.getKey());
                if(Objects.isNull(asset)){
                    continue ;
                }
                String eventMapKey = asset.getIp() + "_" + asset.getId();
                ChangeInfo changeInfo = new ChangeInfo();
                changeInfo.setIsEvent(true);
                changeInfo.setCollectTime(new Date());
                AlarmTempReq alarmTempReq = new AlarmTempReq();
                alarmTempReq.setOrgMsg("资产存在" + days_3 + "天内未确认的三级告警:" + kv.getValue().toString());
                alarmTempReq.setCollectValue(days_3+"天");
                IEvent event = eventInfoChangeManagerService.creatChangeEvent(asset.getId(), changeInfo, StatusInfoChangeTypeEnum.event_unconfirmed_2.getCode(), eventMapKey, -1,alarmTempReq,null,"");
                event.setDescStr("资产存在" + days_3 + "天内未确认的三级告警:" + kv.getValue().toString());
                listenerManager.dispatureEvent(event);
            }

        }
    }

}
