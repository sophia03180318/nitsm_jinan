package com.jcca.component.quartz.station;

import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jcca.admin.biz.entity.StationVersionLog;
import com.jcca.admin.biz.enums.StationVersionStatusEnum;
import com.jcca.admin.biz.service.StationVersionLogService;
import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.utils.AppLogUtils;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;


/**
 * 下达车站更新命令定时任务
 *
 * @author lyp
 */
@Service
@DisallowConcurrentExecution
public class QuartzStationCommandJob extends QuartzJobBean {

    @Resource
    private StationVersionLogService versionLogServ;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        // 调用车站接口开始发起更新jar
        QueryWrapper<StationVersionLog> queryWrapper = new QueryWrapper<StationVersionLog>();
        queryWrapper.eq("STATUS", StationVersionStatusEnum.UPLOAD_OK.name());
        queryWrapper.orderByAsc("CAST(id as integer)");
        List<StationVersionLog> needUploadList = versionLogServ.list(queryWrapper);

        AppLogUtils.buildLogInfo(LogFunctionEnum.CRON_COLLECT_STATUS, "待发起更新车站："+ JSONUtil.toJsonStr(needUploadList),"开始向车站发起jar更新命令~");
        for (StationVersionLog item : needUploadList) {
            versionLogServ.updateJarToStation(item);
            AppLogUtils.buildLogInfo(LogFunctionEnum.CRON_COLLECT_STATUS, JSONUtil.toJsonStr(item),"向车站"+item.getStationId()+"发起jar"+item.getJarName()+"更新命令完成~");
        }
    }

}
