package com.jcca.component.quartz.statistics;

import cn.hutool.core.date.DateUtil;
import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.utils.AppLogUtils;
import com.jcca.common.utils.MyIdUtil;
import com.jcca.web.collect.dao.CollectSensorMapper;
import com.jcca.web.collect.entity.CollectSensor;
import com.jcca.web.statistics.dao.HourTempMapper;
import com.jcca.web.statistics.entity.HourTemp;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * 一小时温度统计
 * 暂时不统计，目前没用了，后续有用可以放开
 *@Slf4j
 *@Service
 *@DisallowConcurrentExecution
 */
public class QuartzHourTempJob extends QuartzJobBean {

    public static boolean open = false;

    @Resource
    private HourTempMapper tempMapper;
    @Resource
    private CollectSensorMapper sensorMapper;

    @Transactional(rollbackFor = Exception.class)
    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        if(!open){
            return ;
        }
        AppLogUtils.buildLogInfo(LogFunctionEnum.CRON_DATA_STATISTICS, DateUtil.formatLocalDateTime(LocalDateTime.now()),"定时任务-温度一小时统计启动");
        List<CollectSensor> collectSensors = sensorMapper.selectMaxValue();
        Date date = new Date();

        for (CollectSensor collectSensor : collectSensors) {
            HourTemp temp = new HourTemp();
            temp.setCreateTime(date);
            temp.setStatisticsYear(DateUtil.year(date));
            temp.setStatisticsMonth(DateUtil.month(date) + 1);
            temp.setStatisticsHour(DateUtil.hour(date, true));
            temp.setStatisticsDay(DateUtil.dayOfMonth(date));
            temp.setTempValue(collectSensor.getValue());
            temp.setAssetId(collectSensor.getAssetId());
            temp.setId(MyIdUtil.getId());
            temp.setEndTime(date);
            tempMapper.insert(temp);
        }
        AppLogUtils.buildLogInfo(LogFunctionEnum.CRON_DATA_STATISTICS, DateUtil.formatLocalDateTime(LocalDateTime.now()),"定时任务-温度一小时统计启动");

    }
}
