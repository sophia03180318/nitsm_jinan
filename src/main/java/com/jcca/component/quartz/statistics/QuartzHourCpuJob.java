package com.jcca.component.quartz.statistics;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.utils.AppLogUtils;
import com.jcca.common.utils.MyIdUtil;
import com.jcca.web.collect.entity.CollectCpu;
import com.jcca.web.collect.service.CollectCpuService;
import com.jcca.web.statistics.entity.HourCpu;
import com.jcca.web.statistics.service.HourCpuService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * CPU一小时统计
 *
 * @author lyp
 */
@Slf4j
@Service
@DisallowConcurrentExecution
public class QuartzHourCpuJob extends QuartzJobBean {

    @Resource
    private CollectCpuService collectCpuServ;
    @Resource
    private HourCpuService hourCpuServ;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {

        AppLogUtils.buildLogInfo(LogFunctionEnum.CRON_DATA_STATISTICS, DateUtil.formatLocalDateTime(LocalDateTime.now()),"定时任务-CPU一小时统计启动");
        Date lastDate = hourCpuServ.lastCreateDate();

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, -30);
        Date now = calendar.getTime();
        QueryWrapper<CollectCpu> collectWrapper = new QueryWrapper<CollectCpu>();

        if (Objects.isNull(lastDate)) {
            collectWrapper.orderByAsc("COLLECT_TIME");
            collectWrapper.lt("CREATE_TIME", now);
            List<CollectCpu> allCollectCpu = collectCpuServ.list(collectWrapper);

            // 按yyyyMMddHH分组
            LinkedHashMap<String, List<CollectCpu>> collectMap = allCollectCpu.stream().filter(bean -> {
                bean.setGroupFlg(DateUtil.format(bean.getCollectTime(), "yyyyMMddHH"));
                return true;
            }).collect(Collectors.groupingBy(CollectCpu::getGroupFlg, LinkedHashMap::new, Collectors.toList()));

            Set<String> timeList = collectMap.keySet();
            for (String time : timeList) {
                List<CollectCpu> collectCpu = collectMap.get(time);
                // 按资产分组
                LinkedHashMap<String, List<CollectCpu>> groupMap = collectCpu.stream().collect(
                        Collectors.groupingBy(CollectCpu::getAssetId, LinkedHashMap::new, Collectors.toList()));
                // 处理数据 最后一次采集时间
                this.statisticsCpuMap(groupMap, now);
            }

        } else {
            List<CollectCpu> statistics = collectCpuServ.statistics(lastDate, now);
            List<HourCpu> hourList = new ArrayList<HourCpu>();
            for (CollectCpu collectCpu : statistics) {
                HourCpu hourData = new HourCpu();
                hourData.setAssetId(collectCpu.getAssetId());
                hourData.setId(MyIdUtil.getId());
                hourData.setCpuUsedRate(collectCpu.getCpuUsedRate());
                Date collectTime = collectCpu.getCollectTime();
                hourData.setStatisticsYear(DateUtil.year(collectTime));
                hourData.setStatisticsMonth(DateUtil.month(collectTime) + 1);
                hourData.setStatisticsHour(DateUtil.hour(collectTime, true));
                hourData.setStatisticsDay(DateUtil.dayOfMonth(collectTime));
                hourData.setEndTime(collectTime);
                hourData.setCreateTime(now);
                hourList.add(hourData);
            }

            if (hourList.isEmpty()) {
                return;
            }
            hourCpuServ.saveBatch(hourList);
        }

        AppLogUtils.buildLogInfo(LogFunctionEnum.CRON_DATA_STATISTICS, DateUtil.formatLocalDateTime(LocalDateTime.now()),"定时任务-CPU一小时统计结束");

    }

    /**
     * 处理保存汇总数据
     *
     * @param groupMap
     * @param now
     */
    private void statisticsCpuMap(LinkedHashMap<String, List<CollectCpu>> groupMap, Date now) {
        Set<String> keySet = groupMap.keySet();
        List<HourCpu> cpuList = new ArrayList<HourCpu>();
        for (String assetId : keySet) {
            List<CollectCpu> dataList = groupMap.get(assetId);
            // 同一个资产一个小时内最后一次采集的时间
            Date collectTime = dataList.get(dataList.size() - 1).getCollectTime();
            double cpuUsedRate = dataList.stream().mapToDouble(CollectCpu::getCpuUsedRate).sum();
            BigDecimal cpuUsedRateBig = new BigDecimal(cpuUsedRate).divide(new BigDecimal(dataList.size()), 2,
                    BigDecimal.ROUND_HALF_UP);

            HourCpu hourData = new HourCpu();
            hourData.setAssetId(assetId);
            hourData.setId(MyIdUtil.getId());
            hourData.setCpuUsedRate(cpuUsedRateBig.doubleValue());
            hourData.setStatisticsYear(DateUtil.year(collectTime));
            hourData.setStatisticsMonth(DateUtil.month(collectTime) + 1);
            hourData.setStatisticsHour(DateUtil.hour(collectTime, true));
            hourData.setStatisticsDay(DateUtil.dayOfMonth(collectTime));
            hourData.setEndTime(collectTime);
            hourData.setCreateTime(now);
            cpuList.add(hourData);
        }

        if (cpuList.isEmpty()) {
            return;
        }

        hourCpuServ.saveBatch(cpuList);
    }

}
