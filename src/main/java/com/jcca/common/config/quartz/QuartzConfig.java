package com.jcca.common.config.quartz;

import com.jcca.component.quartz.alarm.IpmiPortDetectionJob;
import com.jcca.component.quartz.alarm.QuartzUncertainAlarmJob;
import com.jcca.component.quartz.asset.QuartzUpdateAssetJob;
import com.jcca.component.quartz.asset.QuartzUpdateCenterAssetStatusJob;
import com.jcca.component.quartz.clear.QuartzRemoveDBJob;
import com.jcca.component.quartz.clear.QuartzRemoveDataEachMonthJob;
import com.jcca.component.quartz.congxing.QuartzCongxingStatusJob;
import com.jcca.component.quartz.dh.QuartzDhStatusJob;
import com.jcca.component.quartz.mq.QuartzMQStatusJob;
import com.jcca.component.quartz.route.QuartzRouteJob;
import com.jcca.component.quartz.station.*;
import com.jcca.component.quartz.statistics.QuartzHourCpuJob;
import com.jcca.component.quartz.statistics.QuartzHourInterfacesItemJob;
import com.jcca.component.quartz.statistics.QuartzHourInterfacesJob;
import com.jcca.component.quartz.statistics.QuartzHourMemoryJob;
import com.jcca.component.quartz.threeD.QuartzThreeDPushAlarmJob;
import com.jcca.component.quartz.warn.QuartzKeepAlarmWarnJob;
import org.quartz.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 调度任务配置
 *
 * @author lyp
 */
@Configuration
public class QuartzConfig {

    /***
     * 车站通知检查
     * @return
     */
    @Bean
    public JobDetail stationNotifyJobTask() {
        return JobBuilder.newJob(QuartzStationNotifyJob.class).withIdentity(new JobKey("QuartzStationNotifyJob", "STATION_NOTIFY_GROUP"))
                .storeDurably().build();
    }

    @Bean
    public Trigger stationNotifyJobTaskConf() {
        SimpleScheduleBuilder scheduleBuilder = SimpleScheduleBuilder.simpleSchedule().withIntervalInSeconds(360)
                .repeatForever();

        return TriggerBuilder.newTrigger().forJob(stationNotifyJobTask()).withIdentity("QuartzStationNotifyJob", "STATION_NOTIFY_GROUP")
                .withSchedule(scheduleBuilder).build();
    }
    /***
     * 动环告警触发
     * @return
     */
    @Bean
    public JobDetail DHStatusJobTask() {
        return JobBuilder.newJob(QuartzDhStatusJob.class).withIdentity(new JobKey("QuartzDhStatusJob", "DH_GROUP"))
                .storeDurably().build();
    }
    @Bean
    public Trigger DHStatusJobTaskConf() {
        SimpleScheduleBuilder scheduleBuilder = SimpleScheduleBuilder.simpleSchedule().withIntervalInSeconds(60)
                .repeatForever();
        return TriggerBuilder.newTrigger().forJob(DHStatusJobTask()).withIdentity("QuartzDhStatusJob", "DH_GROUP")
                .withSchedule(scheduleBuilder).build();
    }



    /***
     * 3D机房告警推送
     * @return
     */
    @Bean
    public JobDetail threeDPushAlarmJobTask() {
        return JobBuilder.newJob(QuartzThreeDPushAlarmJob.class).withIdentity(new JobKey("QuartzThreeDPushAlarmJob", "THREE_D_ALARM"))
                .storeDurably().build();
    }
    @Bean
    public Trigger threeDPushAlarmJobConf() {
        SimpleScheduleBuilder scheduleBuilder = SimpleScheduleBuilder.simpleSchedule().withIntervalInSeconds(15)
                .repeatForever();

        return TriggerBuilder.newTrigger().forJob(threeDPushAlarmJobTask()).withIdentity("QuartzThreeDPushAlarmJob", "THREE_D_ALARM")
                .withSchedule(scheduleBuilder).build();
    }

    /***
     * 管理口状态检查  2分钟/次
     * @return
     */
    @Bean
    public JobDetail ipmiPortJobTask() {
        return JobBuilder.newJob(IpmiPortDetectionJob.class).withIdentity(new JobKey("QuartzImpiPortStatusJob", "JOB_IPMI_STATUS"))
                .storeDurably().build();
    }

    @Bean
    public Trigger ipmiJobTaskConf() {
        SimpleScheduleBuilder scheduleBuilder = SimpleScheduleBuilder.simpleSchedule().withIntervalInSeconds(240)
                .repeatForever();

        return TriggerBuilder.newTrigger().forJob(ipmiPortJobTask()).withIdentity("QuartzImpiPortStatusJob", "JOB_IPMI_STATUS")
                .withSchedule(scheduleBuilder).build();
    }

    /***
     * 资产监控状态更新  5分钟/次
     * @return
     */
    @Bean
    public JobDetail assetMonitorJobTask() {
        return JobBuilder.newJob(QuartzUpdateAssetJob.class).withIdentity(new JobKey("QuartzAssetMonitorJobTaskJob", "JOB_MONITOR_STATUS"))
                .storeDurably().build();
    }

    @Bean
    public Trigger assetMonitorJobTaskConf() {
        SimpleScheduleBuilder scheduleBuilder = SimpleScheduleBuilder.simpleSchedule().withIntervalInSeconds(300)
                .repeatForever();

        return TriggerBuilder.newTrigger().forJob(assetMonitorJobTask()).withIdentity("QuartzAssetMonitorJobTaskJob", "JOB_MONITOR_STATUS")
                .withSchedule(scheduleBuilder).build();
    }


    /***
     * MQ连接状态检查
     * @return
     */
    /*@Bean
    public JobDetail MQStatusJobTask() {
        return JobBuilder.newJob(QuartzMQStatusJob.class).withIdentity(new JobKey("QuartzMQStatusJob", "MQ_GROUP"))
                .storeDurably().build();
    }

    @Bean
    public Trigger MQStatusJobTaskConf() {
        SimpleScheduleBuilder scheduleBuilder = SimpleScheduleBuilder.simpleSchedule().withIntervalInSeconds(300)
                .repeatForever();
        return TriggerBuilder.newTrigger().forJob(MQStatusJobTask()).withIdentity("QuartzMQStatusJob", "MQ_GROUP")
                .withSchedule(scheduleBuilder).build();
    }*/

    /***
     * 删除日志数据
     * @return
     */
    @Bean
    public JobDetail CongxingDeleteJobTask() {
        return JobBuilder.newJob(QuartzCongxingStatusJob.class).withIdentity(new JobKey("QuartzCongxingStatusJob", "CONGXING_GROUP"))
                .storeDurably().build();
    }

    @Bean
    public Trigger CongxingDeleteTaskConf() {
        SimpleScheduleBuilder scheduleBuilder = SimpleScheduleBuilder.simpleSchedule().withIntervalInSeconds(86400)
                .repeatForever();

        return TriggerBuilder.newTrigger().forJob(CongxingDeleteJobTask()).withIdentity("QuartzCongxingStatusJob", "CONGXING_GROUP")
                .withSchedule(scheduleBuilder).build();
    }


    /***
     * 几天内未确认的告警
     * @return
     */
    @Bean
    public JobDetail uncertainAlarmJobTask() {
        return JobBuilder.newJob(QuartzUncertainAlarmJob.class).withIdentity(new JobKey("QuartzUncertainAlarmJob", "UNCERTAIN_ALARM_GROUP"))
                .storeDurably().build();
    }

    @Bean
    public Trigger uncertainAlarmJobTaskConf() {
        SimpleScheduleBuilder scheduleBuilder = SimpleScheduleBuilder.simpleSchedule().withIntervalInSeconds(86400)
                .repeatForever();

        return TriggerBuilder.newTrigger().forJob(uncertainAlarmJobTask()).withIdentity("QuartzUncertainAlarmJob", "UNCERTAIN_ALARM_GROUP")
                .withSchedule(scheduleBuilder).build();
    }

    @Bean
    public JobDetail collectRouteJobTask() {
        return JobBuilder.newJob(QuartzRouteJob.class).withIdentity(new JobKey("QuartzRouteJob", "COLLECT_GROU"))
                .storeDurably().build();
    }

    /**
     * 一小时执行一次路由表采集 TOPO发现
     *
     * @return
     */
    @Bean
    public Trigger applyTradeTimeOutConf() {
        SimpleScheduleBuilder scheduleBuilder = SimpleScheduleBuilder.simpleSchedule().withIntervalInSeconds(3600)
                .repeatForever();

        return TriggerBuilder.newTrigger().forJob(collectRouteJobTask()).withIdentity("QuartzRouteJob", "COLLECT_GROU")
                .withSchedule(scheduleBuilder).build();
    }

    @Bean
    public JobDetail updateAssetStatusJobTask() {
        return JobBuilder.newJob(QuartzUpdateCenterAssetStatusJob.class)
                .withIdentity(new JobKey("QuartzUpdateCenterAssetStatusJob", "ASSET_STATUS_UPDATE_GROU")).storeDurably()
                .build();
    }

    /**
     * 十分钟执行一次资产状态更新
     *
     * @return
     */
    @Bean
    public Trigger updateAssetStatusJobConf() {
        SimpleScheduleBuilder scheduleBuilder = SimpleScheduleBuilder.simpleSchedule().withIntervalInSeconds(600)
                .repeatForever();

        return TriggerBuilder.newTrigger().forJob(updateAssetStatusJobTask())
                .withIdentity("QuartzUpdateCenterAssetStatusJob", "ASSET_STATUS_UPDATE_GROU")
                .withSchedule(scheduleBuilder).build();
    }

    @Bean
    public JobDetail stationUploadManagerJobTask() {
        return JobBuilder.newJob(QuartzStationUploadManagerJob.class)
                .withIdentity(new JobKey("QuartzStationUploadManagerJob", "STATION_UPLOAD_GROUP")).storeDurably()
                .build();
    }

    /**
     * 20S检测一次车站是否有需要待上传的JAR
     *
     * @return
     */
    @Bean
    public Trigger stationUploadManagerJobConf() {
        SimpleScheduleBuilder scheduleBuilder = SimpleScheduleBuilder.simpleSchedule().withIntervalInSeconds(20)
                .repeatForever();

        return TriggerBuilder.newTrigger().forJob(stationUploadManagerJobTask())
                .withIdentity("QuartzStationUploadManagerJob", "STATION_UPLOAD_GROUP").withSchedule(scheduleBuilder)
                .build();
    }

    @Bean
    public JobDetail stationCommandJobTask() {
        return JobBuilder.newJob(QuartzStationCommandJob.class)
                .withIdentity(new JobKey("QuartzStationCommandJob", "STATION_COMMAND_GROUP")).storeDurably().build();
    }

    /**
     * 30S检测一次车站是否有上传完成等待执行更新命令的车站 有的话发起命令的执行
     *
     * @return
     */
    @Bean
    public Trigger stationCommandJobConf() {
        SimpleScheduleBuilder scheduleBuilder = SimpleScheduleBuilder.simpleSchedule().withIntervalInSeconds(30)
                .repeatForever();

        return TriggerBuilder.newTrigger().forJob(stationCommandJobTask())
                .withIdentity("QuartzStationCommandJob", "STATION_COMMAND_GROUP").withSchedule(scheduleBuilder).build();
    }

    @Bean
    public JobDetail stationUploadStatusQueryJobTask() {
        return JobBuilder.newJob(QuartzStationUploadStatusQueryJob.class)
                .withIdentity(new JobKey("QuartzStationUploadStatusQueryJob", "STATION_QUERY_UPLOAD_STATUS_GROUP")).storeDurably().build();
    }

    /**
     * 30S检测一次车站自动升级的结果
     *
     * @return
     */
    @Bean
    public Trigger stationUploadStatusQueryJobConf() {
        SimpleScheduleBuilder scheduleBuilder = SimpleScheduleBuilder.simpleSchedule().withIntervalInSeconds(30)
                .repeatForever();

        return TriggerBuilder.newTrigger().forJob(stationUploadStatusQueryJobTask())
                .withIdentity("QuartzStationUploadStatusQueryJob", "STATION_QUERY_UPLOAD_STATUS_GROUP").withSchedule(scheduleBuilder).build();
    }

    @Bean
    public JobDetail stationStatusQueryJobTask() {
        return JobBuilder.newJob(QuartzStationStatusQueryJob.class)
                .withIdentity(new JobKey("QuartzStationStatusQueryJob", "STATION_QUERY_STATUS_GROUP")).storeDurably().build();
    }

    /**
     * 5分钟检测一次车站是否在线
     *
     * @return
     */
    @Bean
    public Trigger stationStatusQueryJobConf() {
        SimpleScheduleBuilder scheduleBuilder = SimpleScheduleBuilder.simpleSchedule().withIntervalInSeconds(300)
                .repeatForever();

        return TriggerBuilder.newTrigger().forJob(stationStatusQueryJobTask())
                .withIdentity("QuartzStationStatusQueryJob", "STATION_QUERY_STATUS_GROUP").withSchedule(scheduleBuilder).build();
    }




    @Bean
    public JobDetail keepAlarmWarnJobTask() {
        return JobBuilder.newJob(QuartzKeepAlarmWarnJob.class)
                .withIdentity(new JobKey("QuartzKeepAlarmWarnJob", "TS_ALARM_WARN_GROUP")).storeDurably().build();
    }

    /**
     * 5s检测一次是否持续推告警消息
     * lvyp改为10s一次。因为HHW反馈大屏推送socket被长时间占用。
     *
     * @return
     */
    @Bean
    public Trigger keepAlarmWarnJobConf() {
        SimpleScheduleBuilder scheduleBuilder = SimpleScheduleBuilder.simpleSchedule().withIntervalInSeconds(10)
                .repeatForever();

        return TriggerBuilder.newTrigger().forJob(keepAlarmWarnJobTask())
                .withIdentity("QuartzKeepAlarmWarnJob", "TS_ALARM_WARN_GROUP").withSchedule(scheduleBuilder).build();
    }

    @Bean
    public JobDetail removeDBJobTask() {
        return JobBuilder.newJob(QuartzRemoveDBJob.class)
                .withIdentity(new JobKey("QuartzRemoveDBJob", "CLEAR_DB_GROUP")).storeDurably().build();
    }


    /**
     * 2小时清空一次数据库
     *
     * @return
     */
    @Bean
    public Trigger removeDBJobTaskConf() {
        SimpleScheduleBuilder scheduleBuilder = SimpleScheduleBuilder.simpleSchedule().withIntervalInSeconds(7200)
                .repeatForever();

        return TriggerBuilder.newTrigger().forJob(removeDBJobTask())
                .withIdentity("QuartzRemoveDBJob", "CLEAR_DB_GROUP").withSchedule(scheduleBuilder).build();
    }

    @Bean
    public JobDetail hourCpuJobTask() {
        return JobBuilder.newJob(QuartzHourCpuJob.class)
                .withIdentity(new JobKey("QuartzHourCpuJob", "STATISTICS_CPU_GROUP")).storeDurably().build();
    }

    /**
     * CPU1小时统计
     *
     * @return
     */
    @Bean
    public Trigger hourCpuJobTaskConf() {
        SimpleScheduleBuilder scheduleBuilder = SimpleScheduleBuilder.simpleSchedule().withIntervalInSeconds(3600)
                .repeatForever();

        return TriggerBuilder.newTrigger().forJob(hourCpuJobTask())
                .withIdentity("QuartzHourCpuJob", "STATISTICS_CPU_GROUP").withSchedule(scheduleBuilder).build();
    }

    @Bean
    public JobDetail hourInterfacesItemJobTask() {
        return JobBuilder.newJob(QuartzHourInterfacesItemJob.class)
                .withIdentity(new JobKey("QuartzHourInterfacesItemJob", "STATISTICS_INTERFACESITEM_GROUP")).storeDurably().build();
    }

    /**
     * 端口明细1小时统计
     *
     * @return
     */
    @Bean
    public Trigger hourInterfacesItemJobConf() {
        SimpleScheduleBuilder scheduleBuilder = SimpleScheduleBuilder.simpleSchedule().withIntervalInSeconds(3600)
                .repeatForever();

        return TriggerBuilder.newTrigger().forJob(hourInterfacesItemJobTask())
                .withIdentity("QuartzHourInterfacesItemJob", "STATISTICS_INTERFACESITEM_GROUP").withSchedule(scheduleBuilder).build();
    }

    @Bean
    public JobDetail hourInterfacesJobTask() {
        return JobBuilder.newJob(QuartzHourInterfacesJob.class)
                .withIdentity(new JobKey("QuartzHourInterfacesJob", "STATISTICS_INTERFACES_GROUP")).storeDurably().build();
    }

    /**
     * 端口1小时统计
     *
     * @return
     */
    @Bean
    public Trigger hourInterfacesJobConf() {
        SimpleScheduleBuilder scheduleBuilder = SimpleScheduleBuilder.simpleSchedule().withIntervalInSeconds(3600)
                .repeatForever();

        return TriggerBuilder.newTrigger().forJob(hourInterfacesJobTask())
                .withIdentity("QuartzHourInterfacesJob", "STATISTICS_INTERFACES_GROUP").withSchedule(scheduleBuilder).build();
    }

    /**
     * 内存1小时任务
     *
     * @return
     */
    @Bean
    public JobDetail hourMemoryJobTask() {
        return JobBuilder.newJob(QuartzHourMemoryJob.class)
                .withIdentity(new JobKey("QuartzHourMemoryJob", "STATISTICS_MEMORY_GROUP")).storeDurably().build();
    }

    /**
     * 内存1小时统计
     *
     * @return
     */
    @Bean
    public Trigger hourMemoryJobConf() {
        SimpleScheduleBuilder scheduleBuilder = SimpleScheduleBuilder.simpleSchedule().withIntervalInSeconds(3600)
                .repeatForever();

        return TriggerBuilder.newTrigger().forJob(hourMemoryJobTask())
                .withIdentity("QuartzHourMemoryJob", "STATISTICS_MEMORY_GROUP").withSchedule(scheduleBuilder).build();
    }

    /**
     * 每月删除一年前数据
     *
     * @return
     */
    @Bean
    public JobDetail removetDataEachMonthJobDetail() {
        return JobBuilder.newJob(QuartzRemoveDataEachMonthJob.class)
                .withIdentity(new JobKey("QuartzRemoveDataEachMonthJob", "removetDataEachMonthJobGroup")).storeDurably()
                .build();
    }

    @Bean
    public Trigger removetDataEachMonthJobConf() {
        SimpleScheduleBuilder scheduleBuilder = SimpleScheduleBuilder.simpleSchedule().withIntervalInHours(24 * 7)
                .repeatForever();

        return TriggerBuilder.newTrigger().forJob(removetDataEachMonthJobDetail())
                .withIdentity("QuartzRemoveDataEachMonthJob", "removetDataEachMonthJobGroup").withSchedule(scheduleBuilder).build();
    }

    /**
     * 4小时巡检一次
     *
     * @return
     */
//    @Bean
//    public JobDetail inspectRecord() {
//        return JobBuilder.newJob(InspectJob.class)
//                .withIdentity(new JobKey("InspectJob", "InspectJobGroup")).storeDurably()
//                .build();
//    }
//
//    @Bean
//    public Trigger inspectRecordConf() {
//        SimpleScheduleBuilder scheduleBuilder = SimpleScheduleBuilder.simpleSchedule().withIntervalInHours(4)
//                .repeatForever();
//
//        return TriggerBuilder.newTrigger().forJob(inspectRecord())
//                .withIdentity("InspectJob", "InspectJobGroup").withSchedule(scheduleBuilder).build();
//    }

}
