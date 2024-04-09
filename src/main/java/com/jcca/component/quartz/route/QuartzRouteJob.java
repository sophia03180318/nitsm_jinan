package com.jcca.component.quartz.route;

import com.jcca.web.collect.service.impl.topoDiscovery.CollectTopoDiscoveryHandlerService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Slf4j
@Service
@DisallowConcurrentExecution
public class QuartzRouteJob extends QuartzJobBean {

    @Resource
    private CollectTopoDiscoveryHandlerService collectTopoDiscoveryHandlerService;


    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        //拓扑发现开始执行
        boolean isopen = collectTopoDiscoveryHandlerService.isOpenDiscovery();
        if (isopen) {
            log.info("定时任务-开始网络拓扑发现");
            collectTopoDiscoveryHandlerService.getAllNetAssetInfo();
        } else {
            log.info("定时任务-网络拓扑发现未开启");
        }


    }

}
