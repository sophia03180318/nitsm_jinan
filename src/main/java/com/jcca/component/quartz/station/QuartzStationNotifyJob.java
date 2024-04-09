package com.jcca.component.quartz.station;

import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONUtil;
import com.jcca.admin.biz.service.StationService;
import com.jcca.common.input.LogInputUtils;
import com.jcca.common.input.ServerTypeEnum;
import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.utils.AppLogUtils;
import com.jcca.common.utils.EntityBeanUtil;
import com.jcca.component.quartz.station.bean.StationNotifyBean;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 定时通知车站ITSM的PING状态
 *
 * @author lyp
 */
@Slf4j
@Service
@DisallowConcurrentExecution
public class QuartzStationNotifyJob extends QuartzJobBean {

    /**
     * 缓存的需要重新发送通知的信息
     */
    public static List<StationNotifyBean> NOTIFY_QUEUE = new ArrayList<StationNotifyBean>();

    @Resource
    private StationService stationServ;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        if (NOTIFY_QUEUE.isEmpty()) {
            AppLogUtils.buildLogInfo(LogFunctionEnum.CRON_DATA_STATION_PING, DateUtil.formatLocalDateTime(LocalDateTime.now()),"当前没有需要重新进行通知的任务……");
            return;
        }
        AppLogUtils.buildLogInfo(LogFunctionEnum.CRON_DATA_STATION_PING, DateUtil.formatLocalDateTime(LocalDateTime.now()),String.format("通知车站告警定时任务-当前需要重新发送通知数:%s",NOTIFY_QUEUE.size()));

        List<StationNotifyBean> tryNotifyList = EntityBeanUtil.copyList(NOTIFY_QUEUE, StationNotifyBean.class);
        for (StationNotifyBean jsonObject : tryNotifyList) {
            String body = stationServ.sendPostToStation(jsonObject.getUrl(), jsonObject.getOrgId(), JSONUtil.toJsonStr(jsonObject));
            AppLogUtils.buildLogInfo(LogFunctionEnum.CRON_DATA_STATION_PING, jsonObject,"通知车站告警定时任务-状态返回：" + body);


            String errorFlag = "通讯失败";
            if (!body.contains(errorFlag)) {
                NOTIFY_QUEUE.remove(jsonObject);
            }
        }

        AppLogUtils.buildLogInfo(LogFunctionEnum.CRON_DATA_STATION_PING, DateUtil.formatLocalDateTime(LocalDateTime.now()),String.format("通知车站告警定时任务-当前剩余需要重新发送通知数：%s", NOTIFY_QUEUE.size()));
    }

}
