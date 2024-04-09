package com.jcca.component.quartz.inspect;

import com.jcca.web2.service.InspectRecordService;
import org.quartz.JobExecutionContext;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author HanHW
 * @description 巡检任务
 * @className InspectJob
 * @date 2024/1/19 9:20
 * @since 2.1.0.0
 */
@Service
public class InspectJob extends QuartzJobBean {

    @Resource
    private InspectRecordService inspectRecordService;

    @Override
    protected void executeInternal(JobExecutionContext context) {

        inspectRecordService.prepareRecord();

    }

}
