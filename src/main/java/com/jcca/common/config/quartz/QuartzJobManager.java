package com.jcca.common.config.quartz;

import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.utils.AppLogUtils;
import lombok.Getter;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.stereotype.Component;

/**
 * @author: hhw
 * @description: QuartzJobManager主要是用来管理定时任务
 * @date: 2025-02-25  14:12
 * @since: 2.0.11.0
 */
@Getter
@Component
public class QuartzJobManager {

    private final Scheduler scheduler;

    public QuartzJobManager() throws SchedulerException {
        scheduler = StdSchedulerFactory.getDefaultScheduler();
        scheduler.start();
    }


    public void addJob(String jobName, String jobGroup, String cronExpression, Class<? extends Job> jobClass) {
        JobDetail jobDetail = JobBuilder.newJob(jobClass)
                .withIdentity(jobName, jobGroup)
                .build();

        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(jobName, jobGroup)
                .forJob(jobDetail)
                .withSchedule(CronScheduleBuilder.cronSchedule(cronExpression))
                .build();

        try {
            scheduler.scheduleJob(jobDetail, trigger);
        } catch (SchedulerException e) {
            AppLogUtils.buildLogInfo(LogFunctionEnum.XUNJIAN_MANAGE, "添加任务异常", "任务名称：" + jobName + "，任务组：" + jobGroup);

        }
    }

    public void deleteJob(String jobName, String jobGroup) {
        TriggerKey triggerKey = TriggerKey.triggerKey(jobName, jobGroup);
        try {
            if (scheduler.checkExists(triggerKey)) {
                JobKey jobKey = JobKey.jobKey(jobName, jobGroup);
                scheduler.pauseTrigger(triggerKey);
                scheduler.unscheduleJob(triggerKey);
                scheduler.deleteJob(jobKey);
            }
        } catch (SchedulerException e) {
            AppLogUtils.buildLogInfo(LogFunctionEnum.XUNJIAN_MANAGE, "删除任务异常", "任务名称：" + jobName + "，任务组：" + jobGroup);
        }
    }
}
