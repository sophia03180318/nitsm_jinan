package com.jcca.component.quartz.statistics;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.utils.AppLogUtils;
import com.jcca.common.utils.EntityBeanUtil;
import com.jcca.common.utils.MyIdUtil;
import com.jcca.common.utils.constants.AppLogHead;
import com.jcca.web.collect.entity.CollectMemory;
import com.jcca.web.collect.service.CollectMemoryService;
import com.jcca.web.statistics.entity.HourMemory;
import com.jcca.web.statistics.service.HourMemoryService;
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
 * 一小时内存统计
 *
 * @author lyp
 */
@Slf4j
@Service
@DisallowConcurrentExecution
public class QuartzHourMemoryJob extends QuartzJobBean {

    @Resource
    private HourMemoryService hourMemServ;
    @Resource
    private CollectMemoryService collectMemServ;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        AppLogUtils.buildLogInfo(LogFunctionEnum.CRON_DATA_STATISTICS, DateUtil.formatLocalDateTime(LocalDateTime.now()),"定时任务-内存一小时统计启动");

        Date lastCreateDate = hourMemServ.lastCreateDate();

        // 采集的数据往前移30分钟
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, -30);
        Date now = calendar.getTime();
        QueryWrapper<CollectMemory> collectWrapper = new QueryWrapper<CollectMemory>();

        if (Objects.isNull(lastCreateDate)) {
            collectWrapper.orderByAsc("COLLECT_TIME");
            collectWrapper.lt("CREATE_TIME", now);
            List<CollectMemory> collectDatas = collectMemServ.list(collectWrapper);

            // 按yyyyMMddHH分组
            LinkedHashMap<String, List<CollectMemory>> collectMap = collectDatas.stream().filter(bean -> {
                bean.setGroupFlg(DateUtil.format(bean.getCollectTime(), "yyyyMMddHH"));
                return true;
            }).collect(Collectors.groupingBy(CollectMemory::getGroupFlg, LinkedHashMap::new, Collectors.toList()));
            Set<String> timeList = collectMap.keySet();
            for (String time : timeList) {
                List<CollectMemory> collectGroup = collectMap.get(time);
                // 按资产分组
                LinkedHashMap<String, List<CollectMemory>> groupMap = collectGroup.stream().collect(
                        Collectors.groupingBy(CollectMemory::getAssetId, LinkedHashMap::new, Collectors.toList()));
                // 处理数据 最后一次采集时间
                this.statisticsAassetMap(groupMap, now);
            }
        } else {
            List<CollectMemory> statistics = collectMemServ.statistics(lastCreateDate, now);
            List<HourMemory> hourList = new ArrayList<HourMemory>();
            for (CollectMemory item : statistics) {
                HourMemory hourItem = EntityBeanUtil.copy(item, HourMemory.class);
                hourItem.setId(MyIdUtil.getId());
                Date collectTime = item.getCollectTime();
                hourItem.setStatisticsYear(DateUtil.year(collectTime));
                hourItem.setStatisticsMonth(DateUtil.month(collectTime) + 1);
                hourItem.setStatisticsHour(DateUtil.hour(collectTime, true));
                hourItem.setStatisticsDay(DateUtil.dayOfMonth(collectTime));
                hourItem.setEndTime(collectTime);
                hourItem.setCreateTime(now);
                // 一个资产一条
                hourList.add(hourItem);
            }
            if (hourList.isEmpty()) {
                return;
            }

            hourMemServ.saveBatch(hourList);
        }
        AppLogUtils.buildLogInfo(LogFunctionEnum.CRON_DATA_STATISTICS, DateUtil.formatLocalDateTime(LocalDateTime.now()),"定时任务-内存一小时统计结束");

    }

    private void statisticsAassetMap(LinkedHashMap<String, List<CollectMemory>> groupMap, Date now) {
        Set<String> assetSet = groupMap.keySet();
        List<HourMemory> hourList = new ArrayList<HourMemory>();
        for (String assetId : assetSet) {
            List<CollectMemory> assetCollectList = groupMap.get(assetId);
            int size = assetCollectList.size();

            // 同一个资产一个小时内最后一次采集的时间
            CollectMemory lastInterfaces = assetCollectList.get(assetCollectList.size() - 1);
            Date collectTime = lastInterfaces.getCollectTime();

            // 计算平均值
            Long memTotal = assetCollectList.stream().mapToLong(CollectMemory::getMemTotal).sum();
            BigDecimal memTotalBig = new BigDecimal(memTotal).divide(new BigDecimal(size), 0, BigDecimal.ROUND_HALF_UP);
            Long memUsed = assetCollectList.stream().mapToLong(CollectMemory::getMemUsed).sum();
            BigDecimal memUsedBig = new BigDecimal(memUsed).divide(new BigDecimal(size), 0, BigDecimal.ROUND_HALF_UP);
            Long swapTotal = assetCollectList.stream().mapToLong(CollectMemory::getSwapTotal).sum();
            BigDecimal swapTotalBig = new BigDecimal(swapTotal).divide(new BigDecimal(size), 0,
                    BigDecimal.ROUND_HALF_UP);
            Long swapUsed = assetCollectList.stream().mapToLong(CollectMemory::getSwapUsed).sum();
            BigDecimal swapUsedBig = new BigDecimal(swapUsed).divide(new BigDecimal(size), 0, BigDecimal.ROUND_HALF_UP);
            //
            Double memUsedRate = assetCollectList.stream().mapToDouble(CollectMemory::getMemUsedRate).sum();
            BigDecimal memUsedRateBig = new BigDecimal(memUsedRate).divide(new BigDecimal(size), 2,
                    BigDecimal.ROUND_HALF_UP);
            Double swapUsedRate = assetCollectList.stream().mapToDouble(CollectMemory::getSwapUsedRate).sum();
            BigDecimal swapUsedRateBig = new BigDecimal(swapUsedRate).divide(new BigDecimal(size), 2,
                    BigDecimal.ROUND_HALF_UP);

            HourMemory hourItem = EntityBeanUtil.copy(lastInterfaces, HourMemory.class);
            hourItem.setId(MyIdUtil.getId());
            hourItem.setMemTotal(memTotalBig.longValue());
            hourItem.setMemUsed(memUsedBig.longValue());
            hourItem.setMemUsedRate(memUsedRateBig.doubleValue());
            hourItem.setSwapTotal(swapTotalBig.longValue());
            hourItem.setSwapUsed(swapUsedBig.longValue());
            hourItem.setSwapUsedRate(swapUsedRateBig.doubleValue());

            hourItem.setStatisticsYear(DateUtil.year(collectTime));
            hourItem.setStatisticsMonth(DateUtil.month(collectTime) + 1);
            hourItem.setStatisticsHour(DateUtil.hour(collectTime, true));
            hourItem.setStatisticsDay(DateUtil.dayOfMonth(collectTime));
            hourItem.setEndTime(collectTime);
            hourItem.setCreateTime(now);
            // 一个资产一条
            hourList.add(hourItem);
        }

        if (hourList.isEmpty()) {
            return;
        }

        hourMemServ.saveBatch(hourList);
    }

}
