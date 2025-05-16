package com.jcca.component.quartz.inspect;

import com.jcca.common.utils.SpringContextUtil;
import com.jcca.web.xunjian.service.XunjianRecordService;
import com.jcca.web.xunjian.vo.XunjianRecordVo;
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
        XunjianRecordService bean = SpringContextUtil.getBean(XunjianRecordService.class);
        XunjianRecordVo vo = new XunjianRecordVo();
//        vo.setCronUser(operator);
//        vo.setXunjianTarget(name.split("_")[1]);
//        vo.setAutoFlag(2);
//        bean.beginXunjian(vo);
        log.info("定时巡检，巡检人：{}，巡检任务：{}", operator, name);
    }
}
