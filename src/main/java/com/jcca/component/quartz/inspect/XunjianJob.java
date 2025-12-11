package com.jcca.component.quartz.inspect;

import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.utils.AppLogUtils;
import com.jcca.common.utils.MyIdUtil;
import com.jcca.common.utils.SpringContextUtil;
import com.jcca.web2.constant.XunJianConst;
import com.jcca.web2.dto.xunjian.XunjianJobDto;
import com.jcca.web2.entity.XunjianSchedule;
import com.jcca.web2.enums.xunjian.InspectionMode;
import com.jcca.web2.service.XunjianScheduleService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;

/**
 * @author: hhw
 * @description: XunjianJob主要是用来做智能巡检定时巡检
 * @date: 2025-02-25  14:14
 * @since: 2.0.11.0
 */
@Slf4j
public class XunjianJob implements Job {

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        JobKey key = jobExecutionContext.getJobDetail().getKey();
        String name = key.getName();
        String operator = key.getGroup();
        XunjianScheduleService scheduleService = SpringContextUtil.getBean(XunjianScheduleService.class);
        AppLogUtils.buildLogInfo(LogFunctionEnum.XUNJIAN_MANAGE, "定时巡检任务启动，巡检人：" + operator, "巡检任务：" + name);
        XunjianJobDto dto = new XunjianJobDto();
        dto.setAutoFlag(InspectionMode.SCHEDULED.getCode());
        dto.setId(name.split("_")[0]);
        dto.setOperator(operator);
        String inspectRecordId = MyIdUtil.getId(); // 巡检记录ID
        dto.setInspectRecordId(inspectRecordId);
        XunjianSchedule schedule = scheduleService.getById(dto.getId());
        Thread thread = Thread.currentThread();
        XunJianConst.INSPECT_THREAD_MAP.put(schedule.getJobId(), thread);
        scheduleService.beginXunjian(dto);

    }
}
