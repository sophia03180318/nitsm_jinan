package com.jcca.component.quartz.congxing;

import com.baomidou.mybatisplus.extension.toolkit.SqlRunner;
import lombok.extern.log4j.Log4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Service;

/**
 * 从兴日志表删除
 *
 * @author sophia
 */
@Log4j
@Service
@DisallowConcurrentExecution
public class QuartzCongxingStatusJob extends QuartzJobBean {

    @Override
    protected void executeInternal(JobExecutionContext context) {
        try {
            SqlRunner.db().delete("DELETE FROM CONGXING_LOG WHERE TRUNC(OCCUR_TIME) <= TRUNC(SYSDATE) - 2");
            log.info("CONGXING日志清理");
        } catch (Exception e) {
            log.error("CONGXING日志清理失败");
        }
    }
}



