package com.jcca.component.quartz.clear;

import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.utils.AppLogUtils;
import com.jcca.web.collect.service.*;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 定时2小时清理一次采集表
 *
 * @author lyp
 */
@Service
@DisallowConcurrentExecution
public class QuartzRemoveDBJob extends QuartzJobBean {

    private static final Integer REMOVE_HOUR = 2;

    @Resource
    private CollectCpuService collectCpuService;
    @Resource
    private CollectDBService dbService;
    @Resource
    private CollectInterfacesService interfaceService;
    @Resource
    private CollectMemoryService memoryService;
    @Resource
    private CollectPcbService pcbServ;
    @Resource
    private CollectProcessService processServ;
    @Resource
    private CollectRaidService raidService;
    @Resource
    private CollectDsService dsService;
    @Resource
    private CollectSensorService sensorServ;

    @Resource
    private CollectTablespaceService collectTablespaceService;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        Boolean cpuResult = collectCpuService.removeBeforeData(REMOVE_HOUR);
        AppLogUtils.buildLogInfo(LogFunctionEnum.CRON_DATA, "CPU清理", cpuResult);

        Boolean dbResult = dbService.removeBeforeData(REMOVE_HOUR);
        AppLogUtils.buildLogInfo(LogFunctionEnum.CRON_DATA, "db数据库清理", dbResult);

        Boolean interfaceResult = interfaceService.removeBeforeData(REMOVE_HOUR);
        AppLogUtils.buildLogInfo(LogFunctionEnum.CRON_DATA, "端口清理", interfaceResult);

        Boolean memoryResult = memoryService.removeBeforeData(REMOVE_HOUR);
        AppLogUtils.buildLogInfo(LogFunctionEnum.CRON_DATA, "内存清理", memoryResult);

        Boolean tablespace = collectTablespaceService.removeBeforeData(REMOVE_HOUR);
        AppLogUtils.buildLogInfo(LogFunctionEnum.CRON_DATA, "tablespace数据库清理", tablespace);

        Boolean pcbResult = pcbServ.removeBeforeData(REMOVE_HOUR);
        AppLogUtils.buildLogInfo(LogFunctionEnum.CRON_DATA, "PCB清理", pcbResult);

        Boolean processResult = processServ.removeBeforeData(REMOVE_HOUR);
        AppLogUtils.buildLogInfo(LogFunctionEnum.CRON_DATA, "进程清理", processResult);

        Boolean raidResult = raidService.removeBeforeData(REMOVE_HOUR);
        AppLogUtils.buildLogInfo(LogFunctionEnum.CRON_DATA, "raid磁盘阵列清理", raidResult);

        Boolean dsResult = dsService.removeBeforeData(REMOVE_HOUR);
        AppLogUtils.buildLogInfo(LogFunctionEnum.CRON_DATA, "ds磁盘阵列清理", dsResult);

        Boolean sensorResult = sensorServ.removeBeforeData(REMOVE_HOUR);
        AppLogUtils.buildLogInfo(LogFunctionEnum.CRON_DATA, "传感器清理", sensorResult);

    }

}
