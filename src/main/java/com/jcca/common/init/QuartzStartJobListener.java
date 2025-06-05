package com.jcca.common.init;

import com.jcca.common.input.LogInputUtils;
import com.jcca.common.input.ServerTypeEnum;
import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.utils.AppLogUtils;
import com.jcca.dataProcessing.BusinessInfoReceiver;
import com.jcca.dataProcessing.NoThresholdInfoReceiver;
import com.jcca.dataProcessing.ThresholdInfoReceiver;
import com.jcca.dataProcessing.manager.DataProcessManager;
import com.jcca.dataProcessing.manager.impl.AlarmRepoManagerService;
import com.jcca.dataProcessing.manager.impl.ThresholdMangerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.annotation.Order;

import javax.annotation.Resource;

/**
 * 初始化开始调度任务
 *
 * @author
 */
@Order(3)
@Configuration
@Slf4j
public class QuartzStartJobListener implements ApplicationListener<ContextRefreshedEvent> {

    /**
     * syslog和snmp接收地址
     */
    @Value("${project.syslog_snmp.host}")
    private String host;
    @Resource
    private DataProcessManager dataProcessManager;
    @Resource
    private AlarmRepoManagerService alarmRepoManagerService;
    @Resource
    private ThresholdMangerService thresholdMangerService;
    @Resource
    private ThresholdInfoReceiver thresholdInfoReceiver;
    @Resource
    private BusinessInfoReceiver BusinessInfoReceiver;
    @Resource
    private NoThresholdInfoReceiver noThresholdInfoReceiver;

    /**
     * 项目启动后操作
     */
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (LogInputUtils.inputInfo(ServerTypeEnum.SYSTEM_INIT)) {
            log.info(LogInputUtils.formattingInfoLog(ServerTypeEnum.SYSTEM_INIT, "", "项目启动开始初始化队列任务"));
        }


        thresholdMangerService.init();
        AppLogUtils.buildLogInfo(LogFunctionEnum.DEFAULT_CONFIG, "", "ThresholdMangerService初始化完成");
        alarmRepoManagerService.init();
        AppLogUtils.buildLogInfo(LogFunctionEnum.DEFAULT_CONFIG, "", "EventInfoManagerService初始化完成");
        dataProcessManager.init();
        AppLogUtils.buildLogInfo(LogFunctionEnum.DEFAULT_CONFIG, "", "DataProcessManager初始化完成");

        thresholdInfoReceiver.run();
        AppLogUtils.buildLogInfo(LogFunctionEnum.DEFAULT_CONFIG, "", "阈值启动完成");
        BusinessInfoReceiver.run();
        AppLogUtils.buildLogInfo(LogFunctionEnum.DEFAULT_CONFIG, "", "业务启动完成");
        noThresholdInfoReceiver.run();
        AppLogUtils.buildLogInfo(LogFunctionEnum.DEFAULT_CONFIG, "", "非阈值启动完成");
    }

}
