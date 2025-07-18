package com.jcca.component.quartz.asset;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jcca.common.bean.constant.StatusConst;
import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.utils.AppLogUtils;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.asset.service.AssetService;
import com.jcca.web.asset.utils.enums.AssetWatchStatusEnum;
import com.jcca.web.collect.service.CollectLogicService;
import com.jcca.web2.enums.AssetMonitorEnum;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * 更新资产的监控状态
 *
 * @author lyp
 */
@Slf4j
@DisallowConcurrentExecution
@Service
public class QuartzUpdateAssetJob extends QuartzJobBean {

    /**
     * 启动的时候不执行此任务
     * 等待启动后下一次在执行
     */
    public static boolean once = true;
    @Resource
    private AssetService assetService;
    // 采集状态
    @Resource
    private CollectLogicService collectLogicService;


    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        if (once) {
            once = false;
            return;
        }
        QueryWrapper<Asset> queryWrapper = new QueryWrapper<Asset>();
        queryWrapper.ne("STATUS", StatusConst.FREEZED);
        queryWrapper.eq("IS_DEL", StatusConst.OK);
        List<Asset> list = assetService.list(queryWrapper);
        //更新监控状态字段
        for (Asset asset : list) {
            this.monitorStatus(asset);
        }
        AppLogUtils.buildLogInfo(LogFunctionEnum.CRON_DATA, "更新资产监控状态任务结束", DateUtil.now());
    }


    /**
     * 监控状态赋值
     * 0 异常
     * 1  正常
     * 2 未知
     * 3 不监控
     * AssetMonitorEnum
     *
     * @param record
     */
    private void monitorStatus(Asset record) {
        if (record.getWatch() == AssetWatchStatusEnum.WATCH_STATUS_NO.getCode()) {
            record.setMonitorStatus("不监控");
            assetService.updateMonitorStatus(record, AssetMonitorEnum.UNMONITOR.code);
            return;
        }
        Date createTime = record.getCreateTime();
        try {
            Boolean collectStatus = collectLogicService.getCollectStatus(record.getId(), 30);
            if (collectStatus) {
                record.setMonitorStatus("正常");
                assetService.updateMonitorStatus(record, AssetMonitorEnum.NORMAL.code);
            } else {
                long between = DateUtil.between(createTime, new Date(), DateUnit.MINUTE);
                if (between < 20) {
                    // 新加设备允许20分钟内不上数据
                    record.setMonitorStatus("");
                    assetService.updateMonitorStatus(record, AssetMonitorEnum.TEMP_STATE.code);
                } else {
                    record.setMonitorStatus("异常");
                    assetService.updateMonitorStatus(record, AssetMonitorEnum.ABNORMAL.code);
                }
            }
        } catch (Exception e) {
            AppLogUtils.buildLogError(LogFunctionEnum.CRON_DATA, "更新资产监控状态任务异常", e);
            record.setMonitorStatus("未知");
            assetService.updateMonitorStatus(record, AssetMonitorEnum.UNKNOWN.code);
        }
    }


}
