package com.jcca.common.config.quartz;

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


    public void addJob(String jobName, String jobGroup, String cronExpression, Class<? extends Job> jobClass) throws SchedulerException {
        JobDetail jobDetail = JobBuilder.newJob(jobClass)
                .withIdentity(jobName, jobGroup)
                .build();

        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(jobName, jobGroup)
                .withSchedule(CronScheduleBuilder.cronSchedule(cronExpression))
                .build();

        scheduler.scheduleJob(jobDetail, trigger);
    }

    public void deleteJob(String jobName, String jobGroup) throws SchedulerException {
        JobKey jobKey = JobKey.jobKey(jobName, jobGroup);
        scheduler.deleteJob(jobKey);
    }
}
