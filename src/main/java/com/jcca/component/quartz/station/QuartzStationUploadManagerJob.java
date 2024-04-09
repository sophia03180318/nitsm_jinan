package com.jcca.component.quartz.station;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jcca.admin.biz.entity.StationVersionLog;
import com.jcca.admin.biz.enums.StationVersionStatusEnum;
import com.jcca.admin.biz.service.StationVersionLogService;
import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.redis.service.RedisService;
import com.jcca.common.utils.AppLogUtils;
import com.jcca.common.utils.MyIdUtil;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;


/**
 * 车站定时检测并启动升级程序任务
 *
 * @author lyp
 */
@Slf4j
@Service
@DisallowConcurrentExecution
public class QuartzStationUploadManagerJob extends QuartzJobBean {

    public static final String KEY_UPLOAD = "STATION:UPDATE:LOCK:UPLOAD";

    @Resource
    private StationVersionLogService versionLogServ;
    @Resource
    private RedisService redisServ;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        QueryWrapper<StationVersionLog> queryWrapper = new QueryWrapper<StationVersionLog>();
        queryWrapper.eq("STATUS", StationVersionStatusEnum.AWAIT_UPLOADING.name());
        queryWrapper.orderByAsc("id");
        List<StationVersionLog> needUploadList = versionLogServ.list(queryWrapper);

        if (!needUploadList.isEmpty()) {
            Object object = redisServ.get(KEY_UPLOAD);
            if (Objects.nonNull(object)) {
                return;
            }
            StationVersionLog versionLog = needUploadList.get(0);
            // 锁一小时
            redisServ.set(KEY_UPLOAD, MyIdUtil.getId(), 60 * 60L);
            // 启动上传程序
            AppLogUtils.buildLogInfo(LogFunctionEnum.CRON_DATA_STATION_UPDATE,"车站ID："+ versionLog.getStationId(),"车站程序升级开始上传");

            versionLogServ.uploadJarToStation(versionLog);
        }

    }

}
