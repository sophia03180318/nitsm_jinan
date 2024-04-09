package com.jcca.component.quartz.alarm;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jcca.common.input.LogInputUtils;
import com.jcca.common.input.ServerTypeEnum;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.asset.service.AssetService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 管理口检测
 */
@Slf4j
@Service
@DisallowConcurrentExecution
public class IpmiPortDetectionJob extends QuartzJobBean {

    @Resource
    private AssetService assetServ;
    @Resource
    private AlarmJobService jobServ;

    /**
     * 系统启动第一次任务跳过
     */
    private static boolean isOnce = true;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        if (LogInputUtils.inputInfo(ServerTypeEnum.JOB_QUARTZ)) {
            log.info("定时任务-开始执行管理口定时检测");
        }
        if(isOnce){
            isOnce = false;
            return ;
        }
        QueryWrapper<Asset> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("IS_DEL", 1);
        queryWrapper.eq("WATCH", 1);
        queryWrapper.isNotNull("IPMI_IP");
        List<Asset> assetList = assetServ.list(queryWrapper);

        for (Asset asset : assetList) {
            jobServ.pingIpmiPort(asset);
        }

        if (LogInputUtils.inputInfo(ServerTypeEnum.JOB_QUARTZ)) {
            log.info("定时任务-管理口定时检测结束");
        }


    }


}
