package com.jcca.component.quartz.clear;

import com.jcca.admin.biz.controller.DataTransferController;
import com.jcca.admin.system.service.SysActionLogService;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author GodWone
 * @description 每月清理一次数据
 * @className QuartzRemoveDataEachMonth
 * @date 2023/2/8 14:44
 * @since 2.0.0.1
 */
@Service
public class QuartzRemoveDataEachMonthJob extends QuartzJobBean {

    public static boolean once = true;

    @Value("${project.upload.file-path}")
    private String recoverPath;

    @Resource
    private SysActionLogService sysActionLogService;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        if(once){
//            once = false;
            return;
        }
        sysActionLogService.remove5000(DataTransferController.tables, -365, recoverPath);
    }
}
