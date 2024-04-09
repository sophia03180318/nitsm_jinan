package com.jcca.component.quartz.statistics;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.utils.AppLogUtils;
import com.jcca.common.utils.EntityBeanUtil;
import com.jcca.common.utils.constants.AppLogHead;
import com.jcca.web.collect.entity.CollectInterfaces;
import com.jcca.web.collect.service.CollectInterfacesService;
import com.jcca.web.statistics.entity.HourInterfacesItem;
import com.jcca.web.statistics.service.HourInterfacesItemService;
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
import java.util.stream.Stream;

/**
 * 端口明细一小时统计
 *
 * @author lyp
 */
@Slf4j
@Service
@DisallowConcurrentExecution
public class QuartzHourInterfacesItemJob extends QuartzJobBean {

    @Resource
    private HourInterfacesItemService hourItemServ;
    @Resource
    private CollectInterfacesService collectInterfaceServ;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        AppLogUtils.buildLogInfo(LogFunctionEnum.CRON_DATA_STATISTICS, DateUtil.formatLocalDateTime(LocalDateTime.now()),"定时任务-端口明细一小时统计启动");

        Date lastCreateTime = hourItemServ.lastCreateTime();

        // 采集的数据往前移30分钟
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, -30);
        Date now = calendar.getTime();
        QueryWrapper<CollectInterfaces> collectWrapper = new QueryWrapper<CollectInterfaces>();

        if (Objects.isNull(lastCreateTime)) {
            collectWrapper.orderByAsc("COLLECT_TIME");
            List<CollectInterfaces> collectDatas = collectInterfaceServ.list(collectWrapper);
            // 按yyyyMMddHH分组
            LinkedHashMap<String, List<CollectInterfaces>> collectMap = collectDatas.stream().filter(bean -> {
                bean.setGroupFlg(DateUtil.format(bean.getCollectTime(), "yyyyMMddHH"));
                return true;
            }).collect(Collectors.groupingBy(CollectInterfaces::getGroupFlg, LinkedHashMap::new, Collectors.toList()));
            Set<String> timeList = collectMap.keySet();
            for (String time : timeList) {
                List<CollectInterfaces> collectGroup = collectMap.get(time);
                // 按资产分组
                LinkedHashMap<String, List<CollectInterfaces>> groupMap = collectGroup.stream().collect(
                        Collectors.groupingBy(CollectInterfaces::getAssetId, LinkedHashMap::new, Collectors.toList()));
                // 处理数据 最后一次采集时间
                this.statisticsAassetMap(groupMap, now);
            }
        } else {
            collectWrapper.ge("CREATE_TIME", lastCreateTime);
            collectWrapper.lt("CREATE_TIME", now);
            collectWrapper.orderByAsc("COLLECT_TIME");

            List<CollectInterfaces> awaitStatisticsList = collectInterfaceServ.list(collectWrapper);
            // 资产分组
            LinkedHashMap<String, List<CollectInterfaces>> groupMap = awaitStatisticsList.stream().collect(
                    Collectors.groupingBy(CollectInterfaces::getAssetId, LinkedHashMap::new, Collectors.toList()));
            // 处理数据
            this.statisticsAassetMap(groupMap, now);
        }
        AppLogUtils.buildLogInfo(LogFunctionEnum.CRON_DATA_STATISTICS, DateUtil.formatLocalDateTime(LocalDateTime.now()),"定时任务-端口明细一小时统计结束");

    }

    /**
     * 处理保存一小时统计的数据
     *
     * @param groupMap
     * @param now
     */
    private void statisticsAassetMap(LinkedHashMap<String, List<CollectInterfaces>> groupMap, Date now) {
        Set<String> assetSet = groupMap.keySet();
        List<HourInterfacesItem> hourList = new ArrayList<HourInterfacesItem>();
        for (String assetId : assetSet) {
            List<CollectInterfaces> assetCollectList = groupMap.get(assetId);
            Stream<CollectInterfaces> stream = assetCollectList.stream();
            // 最后一次采集的时间
            Date collectTime = assetCollectList.get(assetCollectList.size() - 1).getCollectTime();

            // 按照端口索引分组
            LinkedHashMap<String, List<CollectInterfaces>> pointMap = stream.collect(
                    Collectors.groupingBy(CollectInterfaces::getPortIndex, LinkedHashMap::new, Collectors.toList()));

            this.statisticsInterfaceMap(pointMap, hourList, collectTime, now);
        }
        if (hourList.isEmpty()) {
            return;
        }

        hourItemServ.saveBatch(hourList);
    }

    /**
     * 按端口汇总
     *
     * @param portMap
     */
    private void statisticsInterfaceMap(LinkedHashMap<String, List<CollectInterfaces>> portMap,
                                        List<HourInterfacesItem> hourList, Date collectTime, Date now) {
        Set<String> keySet = portMap.keySet();
        for (String portIndex : keySet) {
            List<CollectInterfaces> portList = portMap.get(portIndex);
            CollectInterfaces lastInterfaces = portList.get(portList.size() - 1);
            int size = portList.size();

            // 计算平均值
            Long portIn = portList.stream().mapToLong(CollectInterfaces::getPortIn).sum();
            BigDecimal portInBig = new BigDecimal(portIn).divide(new BigDecimal(size), 0, BigDecimal.ROUND_HALF_UP);
            Long portOut = portList.stream().mapToLong(CollectInterfaces::getPortOut).sum();
            BigDecimal portOutBig = new BigDecimal(portOut).divide(new BigDecimal(size), 0, BigDecimal.ROUND_HALF_UP);
            Long discardPacketsIn = portList.stream().mapToLong(CollectInterfaces::getDiscardPacketsIn).sum();
            BigDecimal discardPacketsInBig = new BigDecimal(discardPacketsIn).divide(new BigDecimal(size), 0,
                    BigDecimal.ROUND_HALF_UP);
            Long discardPacketsOut = portList.stream().mapToLong(CollectInterfaces::getDiscardPacketsOut).sum();
            BigDecimal discardPacketsOutBig = new BigDecimal(discardPacketsOut).divide(new BigDecimal(size), 0,
                    BigDecimal.ROUND_HALF_UP);
            Long noUnicastPacketsIn = portList.stream().mapToLong(CollectInterfaces::getNoUnicastPacketsIn).sum();
            BigDecimal noUnicastPacketsInBig = new BigDecimal(noUnicastPacketsIn).divide(new BigDecimal(size), 0,
                    BigDecimal.ROUND_HALF_UP);
            Long noUnicastPacketsOut = portList.stream().mapToLong(CollectInterfaces::getNoUnicastPacketsOut).sum();
            BigDecimal noUnicastPacketsOutBig = new BigDecimal(noUnicastPacketsOut).divide(new BigDecimal(size), 0,
                    BigDecimal.ROUND_HALF_UP);
            Long unicastPacketsIn = portList.stream().mapToLong(CollectInterfaces::getUnicastPacketsIn).sum();
            BigDecimal unicastPacketsInBig = new BigDecimal(unicastPacketsIn).divide(new BigDecimal(size), 0,
                    BigDecimal.ROUND_HALF_UP);
            Long unicastPacketsOut = portList.stream().mapToLong(CollectInterfaces::getUnicastPacketsOut).sum();
            BigDecimal unicastPacketsOutBig = new BigDecimal(unicastPacketsOut).divide(new BigDecimal(size), 0,
                    BigDecimal.ROUND_HALF_UP);
            Long errorCodeIn = portList.stream().mapToLong(CollectInterfaces::getErrorCodeIn).sum();
            BigDecimal errorCodeInBig = new BigDecimal(errorCodeIn).divide(new BigDecimal(size), 0,
                    BigDecimal.ROUND_HALF_UP);
            Long errorCodeOut = portList.stream().mapToLong(CollectInterfaces::getErrorCodeOut).sum();
            BigDecimal errorCodeOutBig = new BigDecimal(errorCodeOut).divide(new BigDecimal(size), 0,
                    BigDecimal.ROUND_HALF_UP);
            // 百分率
            Long portSpeed = portList.stream().mapToLong(CollectInterfaces::getPortSpeed).sum();
            BigDecimal portSpeedBig = new BigDecimal(portSpeed).divide(new BigDecimal(size), 0,
                    BigDecimal.ROUND_HALF_UP);
            Long portInSpeed = portList.stream().mapToLong(CollectInterfaces::getPortInSpeed).sum();
            BigDecimal portInSpeedBig = new BigDecimal(portInSpeed).divide(new BigDecimal(size), 0,
                    BigDecimal.ROUND_HALF_UP);
            Long portOutSpeed = portList.stream().mapToLong(CollectInterfaces::getPortOutSpeed).sum();
            BigDecimal portOutSpeedBig = new BigDecimal(portOutSpeed).divide(new BigDecimal(size), 0,
                    BigDecimal.ROUND_HALF_UP);
            Double losePacketsRateOut = portList.stream().mapToDouble(CollectInterfaces::getLosePacketsOutRate).sum();
            BigDecimal losePacketsRateOutBig = new BigDecimal(losePacketsRateOut).divide(new BigDecimal(size), 2,
                    BigDecimal.ROUND_HALF_UP);
            Double losePacketsRateIn = portList.stream().mapToDouble(CollectInterfaces::getLosePacketsInRate).sum();
            BigDecimal losePacketsRateInBig = new BigDecimal(losePacketsRateIn).divide(new BigDecimal(size), 2,
                    BigDecimal.ROUND_HALF_UP);
            Double erroCodeRateOut = portList.stream().mapToDouble(CollectInterfaces::getErroCodeOutRate).sum();
            BigDecimal erroCodeRateOutBig = new BigDecimal(erroCodeRateOut).divide(new BigDecimal(size), 2,
                    BigDecimal.ROUND_HALF_UP);
            Double erroCodeRateIn = portList.stream().mapToDouble(CollectInterfaces::getErroCodeInRate).sum();
            BigDecimal erroCodeRateInBig = new BigDecimal(erroCodeRateIn).divide(new BigDecimal(size), 2,
                    BigDecimal.ROUND_HALF_UP);

            HourInterfacesItem hourItem = EntityBeanUtil.copy(lastInterfaces, HourInterfacesItem.class);
            hourItem.setPortIn(portInBig.longValue());
            hourItem.setPortOut(portOutBig.longValue());
            hourItem.setDiscardPacketsIn(discardPacketsInBig.longValue());
            hourItem.setDiscardPacketsOut(discardPacketsOutBig.longValue());
            hourItem.setNoUnicastPacketsIn(noUnicastPacketsInBig.longValue());
            hourItem.setNoUnicastPacketsOut(noUnicastPacketsOutBig.longValue());
            hourItem.setUnicastPacketsIn(unicastPacketsInBig.longValue());
            hourItem.setUnicastPacketsOut(unicastPacketsOutBig.longValue());
            hourItem.setErrorCodeIn(errorCodeInBig.longValue());
            hourItem.setErrorCodeOut(errorCodeOutBig.longValue());

            hourItem.setPortSpeed(portSpeedBig.longValue());
            hourItem.setPortInSpeed(portInSpeedBig.longValue());
            hourItem.setPortOutSpeed(portOutSpeedBig.longValue());
            hourItem.setLosePacketsRateOut(losePacketsRateOutBig.doubleValue());
            hourItem.setLosePacketsRateIn(losePacketsRateInBig.doubleValue());
            hourItem.setErroCodeRateOut(erroCodeRateOutBig.doubleValue());
            hourItem.setErroCodeRateIn(erroCodeRateInBig.doubleValue());

            hourItem.setStatisticsYear(DateUtil.year(collectTime));
            hourItem.setStatisticsMonth(DateUtil.month(collectTime) + 1);
            hourItem.setStatisticsHour(DateUtil.hour(collectTime, true));
            hourItem.setStatisticsDay(DateUtil.dayOfMonth(collectTime));
            hourItem.setEndTime(collectTime);
            hourItem.setCreateTime(now);
            // 一个资产一条
            hourList.add(hourItem);

        }
    }

}
