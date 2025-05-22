package com.jcca.component.quartz.inspect;

import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.utils.AppLogUtils;
import com.jcca.common.utils.SpringContextUtil;
import com.jcca.web2.dto.XunjianJobDto;
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
        XunjianJobDto dto = new XunjianJobDto();
        dto.setId(name.split("_")[0]);
        dto.setOperator(operator);
        scheduleService.beginXunjian(dto);
        AppLogUtils.buildLogInfo(LogFunctionEnum.XUNJIAN_MANAGE, "定时巡检完成，巡检人：" + operator, "巡检任务：" + name);
    }
}
