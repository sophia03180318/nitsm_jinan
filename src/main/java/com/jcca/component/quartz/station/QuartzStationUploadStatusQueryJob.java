package com.jcca.component.quartz.station;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jcca.admin.biz.entity.StationVersionLog;
import com.jcca.admin.biz.enums.StationVersionStatusEnum;
import com.jcca.admin.biz.service.StationVersionLogService;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 检测执行命令中的车站的更新结果
 *
 * @author lyp
 */
@Service
@DisallowConcurrentExecution
public class QuartzStationUploadStatusQueryJob extends QuartzJobBean {

    @Resource
    private StationVersionLogService versionLogServ;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        // 调用车站接口查询更新情况
        QueryWrapper<StationVersionLog> queryWrapper = new QueryWrapper<StationVersionLog>();
        queryWrapper.eq("STATUS", StationVersionStatusEnum.UPDATEING.name());
        queryWrapper.orderByAsc("CAST(id as integer)");

        List<StationVersionLog> needUploadList = versionLogServ.list(queryWrapper);
        for (StationVersionLog item : needUploadList) {
            versionLogServ.queryUpdateResult(item);
        }

    }

}
